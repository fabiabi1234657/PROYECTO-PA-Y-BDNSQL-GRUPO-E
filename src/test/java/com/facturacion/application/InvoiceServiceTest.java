package com.facturacion.application;

import com.facturacion.domain.model.*;
import com.facturacion.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InvoiceServiceTest {
    private final InvoiceRepositoryPort invoices = mock(InvoiceRepositoryPort.class);
    private final InvoiceBackupRepositoryPort backup = mock(InvoiceBackupRepositoryPort.class);
    private final ProductRepositoryPort products = mock(ProductRepositoryPort.class);
    private final PersonRepositoryPort persons = mock(PersonRepositoryPort.class);
    private final InvoiceService service = new InvoiceService(invoices, backup, products, persons);
    private Invoice input;
    private InvoiceDetail detail;

    @BeforeEach
    void setUp() {
        detail = new InvoiceDetail();
        detail.setProductoId("prod-1");
        detail.setCantidad(3);
        detail.setPrecioUnitario(0.01);
        input = new Invoice();
        input.setClienteDocumento("123");
        input.setDetalles(List.of(detail));
        when(persons.findByDocumento("123")).thenReturn(Optional.of(
                new Person("123", "Cliente real", null, null, "Bogota", "CLIENTE")));
        when(products.findById("prod-1")).thenReturn(Optional.of(
                new Product("prod-1", "Teclado", "RGB", 10.0, 19.99, null, List.of())));
        when(invoices.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });
    }

    @Test
    void computesTotalsAndReplacesClientSuppliedIdentityDateAndState() {
        input.setClienteNombre("Nombre falso");
        input.setFechaEmision(LocalDateTime.of(2000, 1, 1, 0, 0));
        input.setEstado("ANULADA");
        input.setTotal(-100.0);
        LocalDateTime before = LocalDateTime.now(ZoneOffset.UTC);
        Invoice saved = service.createInvoice(input);
        LocalDateTime after = LocalDateTime.now(ZoneOffset.UTC);
        assertEquals(1L, saved.getId());
        assertEquals("Cliente real", saved.getClienteNombre());
        assertEquals("Teclado", detail.getProductoNombre());
        assertEquals(19.99, detail.getPrecioUnitario());
        assertEquals(59.97, saved.getSubtotal());
        assertEquals(11.39, saved.getIva());
        assertEquals(71.36, saved.getTotal());
        assertEquals("EMITIDA", saved.getEstado());
        assertFalse(saved.getFechaEmision().isBefore(before));
        assertFalse(saved.getFechaEmision().isAfter(after));
        verify(backup).backupInvoice(saved);
    }

    @Test
    void rejectsEmptyDetails() {
        input.setDetalles(List.of());
        assertRejected();
    }

    @Test
    void rejectsNullDetail() {
        input.setDetalles(Arrays.asList((InvoiceDetail) null));
        assertRejected();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1, -10})
    void rejectsInvalidQuantity(Integer quantity) {
        detail.setCantidad(quantity);
        assertRejected();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "missing"})
    void rejectsUnknownOrMissingProduct(String id) {
        detail.setProductoId(id);
        assertRejected();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "missing"})
    void rejectsUnknownOrMissingCustomer(String document) {
        input.setClienteDocumento(document);
        assertRejected();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.MAX_VALUE})
    void rejectsInvalidCatalogPrice(Double price) {
        when(products.findById("prod-1")).thenReturn(Optional.of(
                new Product("prod-1", "Teclado", "RGB", 10.0, price, null, List.of())));
        assertRejected();
    }

    private void assertRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.createInvoice(input));
        verifyNoInteractions(invoices, backup);
    }
}
