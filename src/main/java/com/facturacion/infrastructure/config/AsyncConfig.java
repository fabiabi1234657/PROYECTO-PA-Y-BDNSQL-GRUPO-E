package com.facturacion.infrastructure.config;

import com.facturacion.infrastructure.adapter.backup.InvoiceBackupAdapter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Bean ThreadPoolTaskExecutor — nombre exacto: "procesosExecutor"
     * Usado con @Async("procesosExecutor")
     * setQueueCapacity(0) => sin cola, los hilos se lanzan directamente
     */
    @Bean(name = "procesosExecutor")
    public Executor procesosExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("proceso-async-");
        executor.initialize();
        return executor;
    }

    /**
     * ApplicationRunner: lanza procesos concurrentes al iniciar la aplicación.
     * Demuestra en consola que los hilos se intercalan (no determinista).
     */
    @Bean
    public ApplicationRunner startupRunner(InvoiceBackupAdapter backupAdapter) {
        return args -> {
            log.info("==============================================");
            log.info("  Sistema de Facturación — Spring Boot @Async");
            log.info("  Pool: proceso-async-* (core=3, max=10, queue=0)");
            log.info("  Lanzando procesos concurrentes de prueba...");
            log.info("==============================================");

            // Lanzar 3 procesos asíncronos simultáneos para demostrar concurrencia
            backupAdapter.ejecutarProceso("Proceso-Inventario");
            backupAdapter.ejecutarProceso("Proceso-Reportes");
            backupAdapter.ejecutarProceso("Proceso-Notificaciones");

            log.info("[Main] Los 3 procesos fueron lanzados — el hilo principal NO espera.");
            log.info("[Main] Observa en consola cómo se intercalan los mensajes.");
        };
    }
}
