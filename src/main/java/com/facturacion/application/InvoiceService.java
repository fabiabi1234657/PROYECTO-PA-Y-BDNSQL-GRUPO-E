package com.facturacion.application;

import com.facturacion.domain.model.Invoice;
import com.facturacion.domain.model.InvoiceDetail;
import com.facturacion.domain.model.Person;
import com.facturacion.domain.model.Product;
import com.facturacion.domain.port.out.PersonRepositoryPort;
import com.facturacion.domain.port.in.CreateInvoiceUseCase;
import com.facturacion.domain.port.out.InvoiceBackupRepositoryPort;
import com.facturacion.domain.port.out.InvoiceRepositoryPort;
import com.facturacion.domain.port.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class InvoiceService implements CreateInvoiceUseCase {

    private final InvoiceRepositoryPort invoiceRepository;
    private final InvoiceBackupRepositoryPort backupRepository;
    private final ProductRepositoryPort productRepository;
    private final PersonRepositoryPort personRepository;

    public InvoiceService(InvoiceRepositoryPort invoiceRepository,
                          InvoiceBackupRepositoryPort backupRepository,
                          ProductRepositoryPort productRepository,
                          PersonRepositoryPort personRepository) {
        this.invoiceRepository = invoiceRepository;
        this.backupRepository = backupRepository;
        this.productRepository = productRepository;
        this.personRepository = personRepository;
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        if (invoice == null || invoice.getDetalles() == null || invoice.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La factura debe contener al menos un detalle");
        }
        if (invoice.getClienteDocumento() == null || invoice.getClienteDocumento().isBlank()) {
            throw new IllegalArgumentException("Debe indicar el documento del cliente");
        }
        Person cliente = personRepository.findByDocumento(invoice.getClienteDocumento())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        for (InvoiceDetail detail : invoice.getDetalles()) {
            if (detail == null || detail.getCantidad() == null || detail.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor que cero");
            }
            if (detail.getProductoId() == null || detail.getProductoId().isBlank()) {
                throw new IllegalArgumentException("Debe indicar el producto de cada detalle");
            }
            Product product = productRepository.findById(detail.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + detail.getProductoId()));
            Double precio = product.getPrecioVenta();
            if (precio == null || !Double.isFinite(precio) || precio <= 0
                    || !Double.isFinite(precio * detail.getCantidad())
                    || precio * detail.getCantidad() > Long.MAX_VALUE / 100.0) {
                throw new IllegalArgumentException("El producto no tiene un precio válido para esta cantidad");
            }
            detail.setPrecioUnitario(precio);
            detail.setProductoNombre(product.getNombre());
        }
        invoice.setClienteNombre(cliente.getNombre());
        invoice.setFechaEmision(LocalDateTime.now(ZoneOffset.UTC));
        invoice.setEstado("EMITIDA");
        invoice.calcularTotales();

        Invoice saved = invoiceRepository.save(invoice);
        backupRepository.backupInvoice(saved);
        return saved;
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    @Override
    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }
}
