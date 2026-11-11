package com.facturacion.infrastructure.rest;

import com.facturacion.domain.model.Invoice;
import com.facturacion.domain.port.in.CreateInvoiceUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final CreateInvoiceUseCase createInvoiceUseCase;

    public InvoiceController(CreateInvoiceUseCase createInvoiceUseCase) {
        this.createInvoiceUseCase = createInvoiceUseCase;
    }

    @PostMapping
    public Invoice createInvoice(@RequestBody Invoice invoice) {
        return createInvoiceUseCase.createInvoice(invoice);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidInvoice(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", error.getMessage()));
    }

    @GetMapping
    public List<Invoice> getAllInvoices() {
        return createInvoiceUseCase.getAllInvoices();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        return createInvoiceUseCase.getInvoiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
