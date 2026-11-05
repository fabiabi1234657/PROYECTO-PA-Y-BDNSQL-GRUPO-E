package com.facturacion.infrastructure.adapter.backup;

import com.facturacion.domain.model.Invoice;
import com.facturacion.domain.port.out.InvoiceBackupRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Adaptador de backup asíncrono — Arquitectura Hexagonal.
 *
 * @Component  → Spring gestiona este bean
 * @Async("procesosExecutor") → se ejecuta en el ThreadPoolTaskExecutor
 *                              declarado en AsyncConfig, sin bloquear al cliente.
 */
@Component
public class InvoiceBackupAdapter implements InvoiceBackupRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(InvoiceBackupAdapter.class);

    /**
     * Backup de factura al motor analítico — se llama al emitir una factura.
     */
    @Override
    @Async("procesosExecutor")
    public CompletableFuture<Void> backupInvoice(Invoice invoice) {
        log.info("[{}] Iniciando backup de factura ID: {}",
                Thread.currentThread().getName(), invoice.getId());

        try {
            Thread.sleep(2000); // simula latencia del motor analítico/ML
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("[{}] Factura {} respaldada exitosamente.",
                Thread.currentThread().getName(), invoice.getId());

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Proceso genérico asíncrono — usado por ApplicationRunner al iniciar
     * para demostrar concurrencia visible en consola.
     */
    @Async("procesosExecutor")
    public void ejecutarProceso(String nombreProceso) {
        log.info("[{}] INICIO — {}", Thread.currentThread().getName(), nombreProceso);

        try {
            for (int i = 1; i <= 5; i++) {
                log.info("[{}] {} — iteración {}/5",
                        Thread.currentThread().getName(), nombreProceso, i);
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("[{}] FIN — {}", Thread.currentThread().getName(), nombreProceso);
    }
}
