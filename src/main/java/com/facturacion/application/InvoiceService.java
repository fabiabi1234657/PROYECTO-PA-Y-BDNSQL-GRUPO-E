package com.facturacion.application;

import com.facturacion.domain.model.Invoice;
import com.facturacion.domain.model.InvoiceDetail;
import com.facturacion.domain.port.in.CreateInvoiceUseCase;
import com.facturacion.domain.port.out.InvoiceBackupRepositoryPort;
import com.facturacion.domain.port.out.InvoiceRepositoryPort;
import com.facturacion.domain.port.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService implements CreateInvoiceUseCase {

    private final InvoiceRepositoryPort invoiceRepository;
    private final InvoiceBackupRepositoryPort backupRepository;
    private final ProductRepositoryPort productRepository;

    public InvoiceService(InvoiceRepositoryPort invoiceRepository,
                          InvoiceBackupRepositoryPort backupRepository,
                          ProductRepositoryPort productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.backupRepository = backupRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        if (invoice.getDetalles() == null || invoice.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La factura debe contener al menos un detalle");
        }
        for (InvoiceDetail detail : invoice.getDetalles()) {
            if (detail.getProductoId() != null) {
                productRepository.findById(detail.getProductoId()).ifPresent(product -> {
                    detail.setPrecioUnitario(product.getPrecioVenta());
                    detail.setProductoNombre(product.getNombre());
                });
            }
        }
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
