package com.facturacion.application;

import com.facturacion.domain.model.*;
import com.facturacion.domain.port.out.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class GenerarHechosServiceTest {
    private final InvoiceRepositoryPort invoices = mock(InvoiceRepositoryPort.class);
    private final DruidIngestionPort druid = mock(DruidIngestionPort.class);
    private final GenerarHechosService service = new GenerarHechosService(invoices,
            mock(PersonRepositoryPort.class), mock(ProductRepositoryPort.class), druid);

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void reportsActualSubmissionResultAndKeepsUtcTimestamp(boolean accepted) {
        InvoiceDetail detail = new InvoiceDetail();
        detail.setCantidad(2);
        detail.setPrecioUnitario(10.0);
        Invoice invoice = new Invoice();
        LocalDateTime date = LocalDateTime.of(2026, 11, 11, 2, 30);
        invoice.setFechaEmision(date);
        invoice.setDetalles(List.of(detail));
        when(invoices.findAll()).thenReturn(List.of(invoice));
        when(druid.enviarHechosADruid(anyList())).thenAnswer(call -> {
            List<VentaFact> facts = call.getArgument(0);
            assertEquals(date.toInstant(ZoneOffset.UTC), facts.get(0).getTimestamp());
            return accepted;
        });
        ResultadoSincronizacion result = service.ejecutarProcesoHechos();
        assertEquals(accepted, result.exitoso());
        assertEquals(1, result.totalHechos());
        assertEquals(20.0, result.gananciaTotal());
        assertTrue(result.mensaje().contains(accepted ? "pendiente" : "no aceptó"));
    }

    @Test
    void doesNotSubmitWhenThereAreNoFacts() {
        when(invoices.findAll()).thenReturn(List.of());
        ResultadoSincronizacion result = service.ejecutarProcesoHechos();
        assertTrue(result.exitoso());
        assertEquals(0, result.totalHechos());
        verifyNoInteractions(druid);
    }
}
