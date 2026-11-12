package com.facturacion.application;

import com.facturacion.domain.model.*;
import com.facturacion.domain.port.out.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class GenerarHechosServiceTest {
    private final InvoiceRepositoryPort invoices = mock(InvoiceRepositoryPort.class);
    private final DruidIngestionPort druid = mock(DruidIngestionPort.class);
    private final PersonRepositoryPort persons = mock(PersonRepositoryPort.class);
    private final ProductRepositoryPort products = mock(ProductRepositoryPort.class);
    private final GenerarHechosService service = new GenerarHechosService(invoices, persons, products, druid);

    @Test
    void preservesEnrichedFactsOrderStoredValuesAndLookupCache() {
        Person person = new Person("123", "Cliente", null, null, "Mocoa", "MAYORISTA");
        Product product = new Product("p1", "Nombre actual", null, 3.25, 10.0,
                new Supplier("Proveedor", null, null), List.of());
        when(persons.findByDocumento("123")).thenReturn(Optional.of(person));
        when(products.findById("p1")).thenReturn(Optional.of(product));
        InvoiceDetail first = new InvoiceDetail();
        first.setProductoId("p1");
        first.setProductoNombre("Nombre facturado");
        first.setCantidad(3);
        first.setPrecioUnitario(10.0);
        first.setSubtotal(29.99);
        InvoiceDetail second = new InvoiceDetail();
        second.setProductoId("p1");
        Invoice invoice = new Invoice();
        invoice.setId(7L);
        invoice.setClienteDocumento("123");
        invoice.setFechaEmision(LocalDateTime.of(2026, 11, 11, 2, 30));
        invoice.setDetalles(List.of(first, second));
        when(invoices.findAll()).thenReturn(List.of(invoice));
        when(druid.enviarHechosADruid(anyList())).thenReturn(true);

        ResultadoSincronizacion result = service.ejecutarProcesoHechos();
        ArgumentCaptor<List<VentaFact>> captor = ArgumentCaptor.forClass(List.class);
        verify(druid).enviarHechosADruid(captor.capture());
        List<VentaFact> facts = captor.getValue();
        assertEquals(2, result.totalHechos());
        assertEquals(20.25, result.gananciaTotal());
        VentaFact fact = facts.get(0);
        assertEquals(7L, fact.getFacturaId());
        assertEquals("123", fact.getClienteDocumento());
        assertEquals("Cliente", fact.getClienteNombre());
        assertEquals("Mocoa", fact.getClienteCiudad());
        assertEquals("MAYORISTA", fact.getClienteCategoria());
        assertEquals("Nombre facturado", fact.getProductoNombre());
        assertEquals("Proveedor", fact.getProveedorNombre());
        assertEquals(3, fact.getCantidad());
        assertEquals(10.0, fact.getPrecioUnitario());
        assertEquals(3.25, fact.getCostoUnitario());
        assertEquals(29.99, fact.getSubtotal());
        assertEquals(20.25, fact.getGanancia());
        assertEquals("Nombre actual", facts.get(1).getProductoNombre());
        assertEquals(0, facts.get(1).getCantidad());
        assertEquals(0.0, facts.get(1).getPrecioUnitario());
        assertEquals(0.0, facts.get(1).getSubtotal());
        verify(persons, times(1)).findByDocumento("123");
        verify(products, times(1)).findById("p1");
    }

    @Test
    void preservesFallbacksWhenEnrichmentFailsAndSkipsInvoicesWithoutDetails() {
        when(persons.findByDocumento("123")).thenThrow(new IllegalStateException("Cassandra no disponible"));
        when(products.findById("p1")).thenThrow(new IllegalStateException("MongoDB no disponible"));
        InvoiceDetail detail = new InvoiceDetail();
        detail.setProductoId("p1");
        detail.setCantidad(3);
        detail.setPrecioUnitario(1.235);
        Invoice invoice = new Invoice();
        invoice.setClienteDocumento("123");
        invoice.setClienteNombre("Nombre guardado");
        invoice.setDetalles(List.of(detail));
        when(invoices.findAll()).thenReturn(List.of(new Invoice(), invoice));

        service.ejecutarProcesoHechos();

        ArgumentCaptor<List<VentaFact>> captor = ArgumentCaptor.forClass(List.class);
        verify(druid).enviarHechosADruid(captor.capture());
        assertEquals(1, captor.getValue().size());
        VentaFact fact = captor.getValue().getFirst();
        assertEquals("Nombre guardado", fact.getClienteNombre());
        assertEquals("DESCONOCIDA", fact.getClienteCiudad());
        assertEquals("DESCONOCIDA", fact.getClienteCategoria());
        assertNull(fact.getProductoNombre());
        assertEquals("DESCONOCIDO", fact.getProveedorNombre());
        assertEquals(0.0, fact.getCostoUnitario());
        assertEquals(3.71, fact.getSubtotal());
        assertEquals(3.71, fact.getGanancia());
        assertNotNull(fact.getTimestamp());
    }

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
