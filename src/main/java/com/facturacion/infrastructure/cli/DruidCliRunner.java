package com.facturacion.infrastructure.cli;

import com.facturacion.domain.model.VentaFact;
import com.facturacion.domain.port.in.GenerarHechosUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DruidCliRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DruidCliRunner.class);

    private final GenerarHechosUseCase generarHechosUseCase;

    public DruidCliRunner(GenerarHechosUseCase generarHechosUseCase) {
        this.generarHechosUseCase = generarHechosUseCase;
    }

    @Override
    public void run(String... args) {
        boolean requested = Arrays.stream(args)
                .anyMatch(a -> "--generar-hechos".equals(a) || "--druid-sync".equals(a));
        if (!requested) {
            return;
        }
        log.info("=== CLI Druid: iniciando generacion y sincronizacion de hechos ===");
        List<VentaFact> facts = generarHechosUseCase.ejecutarProcesoHechos();
        double ganancia = facts.stream().mapToDouble(f -> f.getGanancia() != null ? f.getGanancia() : 0.0).sum();
        log.info("=== CLI Druid: finalizado. Hechos: {} | Ganancia total: {} ===", facts.size(), ganancia);
    }
}
