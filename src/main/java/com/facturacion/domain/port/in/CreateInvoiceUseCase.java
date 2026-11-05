package com.facturacion.domain.port.in;

import com.facturacion.domain.model.Invoice;
import java.util.List;
import java.util.Optional;

public interface CreateInvoiceUseCase {
    Invoice createInvoice(Invoice invoice);
    List<Invoice> getAllInvoices();
    Optional<Invoice> getInvoiceById(Long id);
}
