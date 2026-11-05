package com.facturacion.infrastructure.config;

import com.facturacion.domain.model.Person;
import com.facturacion.domain.model.Product;
import com.facturacion.domain.model.Supplier;
import com.facturacion.domain.port.out.PersonRepositoryPort;
import com.facturacion.domain.port.out.ProductRepositoryPort;
import com.facturacion.infrastructure.adapter.backup.InvoiceBackupAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "procesosExecutor")
    public Executor procesosExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("proceso-async-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ApplicationRunner startupRunner(InvoiceBackupAdapter backupAdapter,
                                           ProductRepositoryPort productRepository,
                                           PersonRepositoryPort personRepository) {
        return args -> {
            cargarDatosIniciales(productRepository, personRepository);

            log.info("Iniciando pruebas de procesos asincronos en pool proceso-async-*");
            backupAdapter.ejecutarProceso("Proceso-Inventario");
            backupAdapter.ejecutarProceso("Proceso-Reportes");
            backupAdapter.ejecutarProceso("Proceso-Notificaciones");
        };
    }

    private void cargarDatosIniciales(ProductRepositoryPort productRepository,
                                      PersonRepositoryPort personRepository) {
        try {
            if (productRepository.findAll().isEmpty()) {
                Supplier techDist = new Supplier("TechDist", "ventas@techdist.com", "3101234567");
                Supplier gameGear = new Supplier("GameGear", "info@gamegear.co", "3209876543");

                productRepository.save(new Product(null, "Laptop HP", "Laptop 15 pulgadas Intel i5",
                        1500000.0, 2200000.0, techDist, Collections.emptyList()));
                productRepository.save(new Product(null, "Mouse Logitech", "Mouse inalambrico ergonomico",
                        45000.0, 85000.0, techDist, Collections.emptyList()));
                productRepository.save(new Product(null, "Teclado Mecanico", "Teclado mecanico RGB switches blue",
                        120000.0, 220000.0, gameGear, Collections.emptyList()));
                log.info("Productos iniciales registrados en MongoDB");
            }
        } catch (Exception e) {
            log.warn("No se pudieron cargar productos iniciales en MongoDB: {}", e.getMessage());
        }

        try {
            if (personRepository.findAll().isEmpty()) {
                personRepository.save(new Person("1001234567", "Carlos Ramirez",
                        "carlos@gmail.com", "3151234567", "Bogota", "CLIENTE"));
                personRepository.save(new Person("1009876543", "Ana Torres",
                        "ana@hotmail.com", "3167654321", "Medellin", "CLIENTE"));
                personRepository.save(new Person("1005551234", "Luis Gomez",
                        "luis@empresa.co", "3123334444", "Cali", "EMPLEADO"));
                log.info("Personas iniciales registradas en Cassandra");
            }
        } catch (Exception e) {
            log.warn("No se pudieron cargar personas iniciales en Cassandra: {}", e.getMessage());
        }
    }
}
