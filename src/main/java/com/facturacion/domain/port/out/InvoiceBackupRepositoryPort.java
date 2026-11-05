package com.facturacion.domain.port.out;

import com.facturacion.domain.model.Invoice;
import java.util.concurrent.CompletableFuture;

public interface InvoiceBackupRepositoryPort {
    CompletableFuture<Void> backupInvoice(Invoice invoice);
}
