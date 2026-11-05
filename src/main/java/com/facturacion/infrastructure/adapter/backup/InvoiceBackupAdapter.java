package com.facturacion.infrastructure.adapter.backup;

import com.facturacion.domain.model.Invoice;
import com.facturacion.domain.port.out.InvoiceBackupRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class InvoiceBackupAdapter implements InvoiceBackupRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(InvoiceBackupAdapter.class);

    @Override
    @Async("procesosExecutor")
    public CompletableFuture<Void> backupInvoice(Invoice invoice) {
        log.info("[{}] Iniciando respaldo de factura ID: {}",
                Thread.currentThread().getName(), invoice.getId());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("[{}] Factura {} respaldada correctamente.",
                Thread.currentThread().getName(), invoice.getId());

        return CompletableFuture.completedFuture(null);
    }

    @Async("procesosExecutor")
    public void ejecutarProceso(String nombreProceso) {
        log.info("[{}] Inicio {}", Thread.currentThread().getName(), nombreProceso);

        try {
            for (int i = 1; i <= 5; i++) {
                log.info("[{}] {} paso {}/5",
                        Thread.currentThread().getName(), nombreProceso, i);
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("[{}] Fin {}", Thread.currentThread().getName(), nombreProceso);
    }
}
