package com.facturacion.application;

import com.facturacion.domain.model.Invoice;
import com.facturacion.domain.model.InvoiceDetail;
import com.facturacion.domain.model.Product;
import com.facturacion.domain.port.out.InvoiceBackupRepositoryPort;
import com.facturacion.domain.port.out.InvoiceRepositoryPort;
import com.facturacion.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InvoiceServiceTest {

    @Test
    void createInvoiceComputesServerSideTotalsAndTriggersAsyncBackup() {
        InvoiceRepositoryPort invoiceRepo = mock(InvoiceRepositoryPort.class);
        InvoiceBackupRepositoryPort backupRepo = mock(InvoiceBackupRepositoryPort.class);
        ProductRepositoryPort productRepo = mock(ProductRepositoryPort.class);

        when(productRepo.findById("prod-1")).thenReturn(Optional.of(
                new Product("prod-1", "Teclado Mecanico", "RGB", 120000.0, 19.99, null, List.of())
        ));
        when(invoiceRepo.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });
        when(backupRepo.backupInvoice(any(Invoice.class))).thenReturn(CompletableFuture.completedFuture(null));

        InvoiceService service = new InvoiceService(invoiceRepo, backupRepo, productRepo);

        InvoiceDetail detail = new InvoiceDetail();
        detail.setProductoId("prod-1");
        detail.setCantidad(3);
        detail.setPrecioUnitario(0.01); // tampered client price must be overwritten

        Invoice input = new Invoice();
        input.setClienteDocumento("1001234567");
        input.setClienteNombre("Carlos Ramirez");
        input.setDetalles(List.of(detail));

        Invoice saved = service.createInvoice(input);

        assertEquals(1L, saved.getId());
        assertEquals("Teclado Mecanico", saved.getDetalles().get(0).getProductoNombre());
        assertEquals(19.99, saved.getDetalles().get(0).getPrecioUnitario());
        assertEquals(59.97, saved.getSubtotal());
        assertEquals(11.39, saved.getIva());
        assertEquals(71.36, saved.getTotal());
        assertEquals("EMITIDA", saved.getEstado());
        assertNotNull(saved.getFechaEmision());
        verify(backupRepo, times(1)).backupInvoice(saved);
    }

    @Test
    void createInvoiceRejectsEmptyDetails() {
        InvoiceService service = new InvoiceService(
                mock(InvoiceRepositoryPort.class),
                mock(InvoiceBackupRepositoryPort.class),
                mock(ProductRepositoryPort.class)
        );
        Invoice empty = new Invoice();
        assertThrows(IllegalArgumentException.class, () -> service.createInvoice(empty));
    }
}
