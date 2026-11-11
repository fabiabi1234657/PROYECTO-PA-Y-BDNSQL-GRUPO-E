package com.facturacion.application;

import com.facturacion.domain.model.Invoice;
import com.facturacion.domain.model.InvoiceDetail;
import com.facturacion.domain.model.Person;
import com.facturacion.domain.model.Product;
import com.facturacion.domain.model.VentaFact;
import com.facturacion.domain.model.ResultadoSincronizacion;
import com.facturacion.domain.port.in.GenerarHechosUseCase;
import com.facturacion.domain.port.out.DruidIngestionPort;
import com.facturacion.domain.port.out.InvoiceRepositoryPort;
import com.facturacion.domain.port.out.PersonRepositoryPort;
import com.facturacion.domain.port.out.ProductRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GenerarHechosService implements GenerarHechosUseCase {

    private static final Logger log = LoggerFactory.getLogger(GenerarHechosService.class);

    private final InvoiceRepositoryPort invoiceRepository;
    private final PersonRepositoryPort personRepository;
    private final ProductRepositoryPort productRepository;
    private final DruidIngestionPort druidIngestionPort;

    public GenerarHechosService(InvoiceRepositoryPort invoiceRepository,
                                PersonRepositoryPort personRepository,
                                ProductRepositoryPort productRepository,
                                DruidIngestionPort druidIngestionPort) {
        this.invoiceRepository = invoiceRepository;
        this.personRepository = personRepository;
        this.productRepository = productRepository;
        this.druidIngestionPort = druidIngestionPort;
    }

    @Override
    public ResultadoSincronizacion ejecutarProcesoHechos() {
        log.info("[ETL] Extrayendo facturas desde MySQL...");
        List<Invoice> invoices = invoiceRepository.findAll();
        log.info("[ETL] {} facturas extraidas", invoices.size());

        List<VentaFact> facts = consolidarHechos(invoices);
        log.info("[ETL] Consolidacion completada: {} hechos VentaFact generados", facts.size());

        if (facts.isEmpty()) {
            log.warn("[ETL] No hay hechos para enviar a Druid");
            return new ResultadoSincronizacion(true, 0, 0, "No hay hechos para enviar a Druid");
        }

        log.info("[ETL] Enviando {} hechos a Druid...", facts.size());
        boolean ok = druidIngestionPort.enviarHechosADruid(facts);
        if (ok) {
            log.info("[ETL] Envio a Druid exitoso");
        } else {
            log.error("[ETL] Fallo el envio a Druid");
        }
        double ganancia = facts.stream().mapToDouble(f -> f.getGanancia() != null ? f.getGanancia() : 0).sum();
        return new ResultadoSincronizacion(ok, facts.size(), ganancia, ok
                ? "Ingesta enviada a Druid; pendiente de finalización"
                : "Druid no aceptó la ingesta o no fue posible conectar; consulte el registro del servidor");
    }

    private List<VentaFact> consolidarHechos(List<Invoice> invoices) {
        // Caches para evitar consultas repetidas a Cassandra / MongoDB
        Map<String, Person> personCache = new HashMap<>();
        Map<String, Product> productCache = new HashMap<>();

        List<VentaFact> facts = new ArrayList<>();
        for (Invoice invoice : invoices) {
            if (invoice.getDetalles() == null) {
                continue;
            }
            Person person = lookupPerson(invoice.getClienteDocumento(), personCache);
            Instant ts = toInstant(invoice.getFechaEmision());

            for (InvoiceDetail d : invoice.getDetalles()) {
                Product product = lookupProduct(d.getProductoId(), productCache);

                facts.add(crearHecho(invoice, d, person, ts, product));
            }
        }
        return facts;
    }

    private VentaFact crearHecho(Invoice invoice, InvoiceDetail d, Person person,
                                 Instant ts, Product product) {
        int cantidad = d.getCantidad() != null ? d.getCantidad() : 0;
        double precio = d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0.0;
        double costo = product != null && product.getCosto() != null ? product.getCosto() : 0.0;
        double subtotal = d.getSubtotal() != null ? d.getSubtotal() : round2(cantidad * precio);
        double ganancia = round2((precio - costo) * cantidad);

        String productoNombre = d.getProductoNombre();
        if (productoNombre == null && product != null) {
            productoNombre = product.getNombre();
        }
        String proveedor = product != null && product.getProveedor() != null
                ? product.getProveedor().getNombre() : "DESCONOCIDO";

        return new VentaFact(
                ts,
                invoice.getId(),
                invoice.getClienteDocumento(),
                person != null ? person.getNombre() : invoice.getClienteNombre(),
                person != null && person.getCiudad() != null ? person.getCiudad() : "DESCONOCIDA",
                person != null && person.getCategoria() != null ? person.getCategoria() : "DESCONOCIDA",
                d.getProductoId(),
                productoNombre,
                proveedor,
                cantidad, precio, costo, subtotal, ganancia);
    }

    private Person lookupPerson(String documento, Map<String, Person> cache) {
        if (documento == null) {
            return null;
        }
        return cache.computeIfAbsent(documento, k -> {
            try {
                return personRepository.findByDocumento(k).orElse(null);
            } catch (Exception e) {
                log.warn("[ETL] No se pudo obtener cliente {}: {}", k, e.getMessage());
                return null;
            }
        });
    }

    private Product lookupProduct(String productoId, Map<String, Product> cache) {
        if (productoId == null) {
            return null;
        }
        return cache.computeIfAbsent(productoId, k -> {
            try {
                return productRepository.findById(k).orElse(null);
            } catch (Exception e) {
                log.warn("[ETL] No se pudo obtener producto {}: {}", k, e.getMessage());
                return null;
            }
        });
    }

    private static Instant toInstant(LocalDateTime date) {
        LocalDateTime d = date != null ? date : LocalDateTime.now(ZoneOffset.UTC);
        return d.toInstant(ZoneOffset.UTC);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
