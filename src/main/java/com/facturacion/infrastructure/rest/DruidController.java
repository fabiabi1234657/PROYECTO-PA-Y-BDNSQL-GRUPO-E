package com.facturacion.infrastructure.rest;

import com.facturacion.domain.model.ResultadoSincronizacion;
import com.facturacion.domain.port.in.GenerarHechosUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/druid")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class DruidController {
    private static final Logger log = LoggerFactory.getLogger(DruidController.class);

    private final GenerarHechosUseCase generarHechosUseCase;

    public DruidController(GenerarHechosUseCase generarHechosUseCase) {
        this.generarHechosUseCase = generarHechosUseCase;
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sincronizarHechos() {
        ResultadoSincronizacion resultado = generarHechosUseCase.ejecutarProcesoHechos();

        String estado;
        int codigo;
        if (resultado.exitoso()) {
            if (resultado.totalHechos() == 0) {
                estado = "NO_DATA";
                codigo = 200;
            } else {
                estado = "SUBMITTED";
                codigo = 202;
            }
        } else {
            estado = "ERROR";
            codigo = 502;
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", estado);
        resp.put("hechosEnviados", resultado.exitoso() ? resultado.totalHechos() : 0);
        resp.put("gananciaTotal", resultado.gananciaTotal());
        resp.put("mensaje", resultado.mensaje());
        resp.put("message", resultado.mensaje());
        return ResponseEntity.status(codigo).body(resp);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> errorProceso(RuntimeException error) {
        log.error("Error ejecutando el proceso ETL de Druid", error);
        return ResponseEntity.internalServerError().body(Map.of(
                "status", "ERROR", "message", "No se pudo ejecutar el proceso ETL; consulte el registro del servidor"));
    }
}
