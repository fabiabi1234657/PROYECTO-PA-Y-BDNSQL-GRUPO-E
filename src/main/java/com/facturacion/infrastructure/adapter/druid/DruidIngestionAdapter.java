package com.facturacion.infrastructure.adapter.druid;

import com.facturacion.domain.model.VentaFact;
import com.facturacion.domain.port.out.DruidIngestionPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class DruidIngestionAdapter implements DruidIngestionPort {

    private static final Logger log = LoggerFactory.getLogger(DruidIngestionAdapter.class);
    static final String DATASOURCE = "hechos_ventas";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final String coordinatorUrl;

    public DruidIngestionAdapter(ObjectMapper objectMapper,
                                 @Value("${druid.coordinator-url}") String coordinatorUrl) {
        this.objectMapper = objectMapper;
        this.coordinatorUrl = coordinatorUrl;
    }

    @Override
    public boolean enviarHechosADruid(List<VentaFact> facts) {
        if (facts == null || facts.isEmpty()) {
            log.warn("[Druid] Lista vacia, nada que enviar");
            return false;
        }
        try {
            String body = objectMapper.writeValueAsString(buildTaskSpec(facts));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String url = coordinatorUrl + "/druid/indexer/v1/task";
            log.info("[Druid] POST {} ({} filas)", url, facts.size());

            ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            boolean ok = response.getStatusCode().is2xxSuccessful();
            log.info("[Druid] Respuesta {}: {}", response.getStatusCode(), response.getBody());
            return ok;
        } catch (Exception e) {
            log.error("[Druid] Error enviando tarea de ingesta: {}", e.getMessage());
            return false;
        }
    }

    private ObjectNode buildTaskSpec(List<VentaFact> facts) throws Exception {
        StringBuilder data = new StringBuilder();
        for (VentaFact f : facts) {
            ObjectNode row = objectMapper.createObjectNode();
            row.put("timestamp", f.getTimestamp().toString());
            row.put("facturaId", f.getFacturaId() != null ? f.getFacturaId() : 0L);
            row.put("clienteDocumento", f.getClienteDocumento());
            row.put("clienteNombre", f.getClienteNombre());
            row.put("clienteCiudad", f.getClienteCiudad());
            row.put("clienteCategoria", f.getClienteCategoria());
            row.put("productoId", f.getProductoId());
            row.put("productoNombre", f.getProductoNombre());
            row.put("proveedorNombre", f.getProveedorNombre());
            row.put("cantidad", f.getCantidad() != null ? f.getCantidad() : 0);
            row.put("precioUnitario", f.getPrecioUnitario() != null ? f.getPrecioUnitario() : 0.0);
            row.put("costoUnitario", f.getCostoUnitario() != null ? f.getCostoUnitario() : 0.0);
            row.put("subtotal", f.getSubtotal() != null ? f.getSubtotal() : 0.0);
            row.put("ganancia", f.getGanancia() != null ? f.getGanancia() : 0.0);
            data.append(objectMapper.writeValueAsString(row)).append('\n');
        }

        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "index_parallel");
        ObjectNode spec = root.putObject("spec");

        ObjectNode dataSchema = spec.putObject("dataSchema");
        dataSchema.put("dataSource", DATASOURCE);
        dataSchema.putObject("timestampSpec").put("column", "timestamp").put("format", "iso");

        ArrayNode dims = dataSchema.putObject("dimensionsSpec").putArray("dimensions");
        dims.addObject().put("type", "long").put("name", "facturaId");
        for (String d : List.of("clienteDocumento", "clienteNombre", "clienteCiudad", "clienteCategoria",
                "productoId", "productoNombre", "proveedorNombre")) {
            dims.add(d);
        }

        ArrayNode metrics = dataSchema.putArray("metricsSpec");
        metrics.addObject().put("type", "longSum").put("name", "cantidad").put("fieldName", "cantidad");
        for (String m : List.of("precioUnitario", "costoUnitario", "subtotal", "ganancia")) {
            metrics.addObject().put("type", "doubleSum").put("name", m).put("fieldName", m);
        }
        metrics.addObject().put("type", "count").put("name", "count");

        dataSchema.putObject("granularitySpec")
                .put("type", "uniform")
                .put("segmentGranularity", "day")
                .put("queryGranularity", "none")
                .put("rollup", false);

        ObjectNode ioConfig = spec.putObject("ioConfig");
        ioConfig.put("type", "index_parallel");
        ioConfig.putObject("inputSource").put("type", "inline").put("data", data.toString());
        ioConfig.putObject("inputFormat").put("type", "json");
        ioConfig.put("appendToExisting", false);

        spec.putObject("tuningConfig").put("type", "index_parallel").put("maxRowsPerSegment", 5000000);
        return root;
    }
}
