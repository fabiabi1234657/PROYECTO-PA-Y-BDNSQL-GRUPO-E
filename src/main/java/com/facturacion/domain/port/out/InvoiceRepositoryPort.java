package com.facturacion.domain.port.out;

import com.facturacion.domain.model.Invoice;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepositoryPort {
    Invoice save(Invoice invoice);
    List<Invoice> findAll();
    Optional<Invoice> findById(Long id);
}
