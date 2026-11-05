package com.facturacion.application;

import com.facturacion.domain.model.Invoice;
import com.facturacion.domain.port.in.CreateInvoiceUseCase;
import com.facturacion.domain.port.out.InvoiceRepositoryPort;
import com.facturacion.domain.port.out.InvoiceBackupRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService implements CreateInvoiceUseCase {

    private final InvoiceRepositoryPort invoiceRepository;
    private final InvoiceBackupRepositoryPort backupRepository;

    public InvoiceService(InvoiceRepositoryPort invoiceRepository,
                          InvoiceBackupRepositoryPort backupRepository) {
        this.invoiceRepository = invoiceRepository;
        this.backupRepository = backupRepository;
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        Invoice saved = invoiceRepository.save(invoice);
        // Backup asíncrono — no bloquea la respuesta al cliente
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
