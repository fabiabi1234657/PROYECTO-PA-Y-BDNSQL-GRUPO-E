package com.facturacion.infrastructure.adapter.mysql;

import com.facturacion.domain.model.Invoice;
import com.facturacion.domain.model.InvoiceDetail;
import com.facturacion.domain.port.out.InvoiceRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InvoiceMysqlAdapter implements InvoiceRepositoryPort {

    private final InvoiceJpaRepository jpaRepository;

    public InvoiceMysqlAdapter(InvoiceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceEntity entity = toEntity(invoice);
        InvoiceEntity saved = jpaRepository.save(entity);
        return toModel(saved);
    }

    @Override
    public List<Invoice> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return jpaRepository.findById(id).map(this::toModel);
    }

    private InvoiceEntity toEntity(Invoice invoice) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setClienteDocumento(invoice.getClienteDocumento());
        entity.setClienteNombre(invoice.getClienteNombre());
        entity.setFechaEmision(invoice.getFechaEmision());
        entity.setSubtotal(invoice.getSubtotal());
        entity.setIva(invoice.getIva());
        entity.setTotal(invoice.getTotal());
        entity.setEstado(invoice.getEstado());

        List<InvoiceDetail> items = invoice.getDetalles() != null ? invoice.getDetalles() : Collections.emptyList();
        List<InvoiceDetailEntity> detalles = items.stream().map(d -> {
            InvoiceDetailEntity de = new InvoiceDetailEntity();
            de.setProductoId(d.getProductoId());
            de.setProductoNombre(d.getProductoNombre());
            de.setCantidad(d.getCantidad());
            de.setPrecioUnitario(d.getPrecioUnitario());
            de.setSubtotal(d.getSubtotal());
            de.setFactura(entity);
            return de;
        }).collect(Collectors.toList());

        entity.setDetalles(detalles);
        return entity;
    }

    private Invoice toModel(InvoiceEntity entity) {
        Invoice invoice = new Invoice();
        invoice.setId(entity.getId());
        invoice.setClienteDocumento(entity.getClienteDocumento());
        invoice.setClienteNombre(entity.getClienteNombre());
        invoice.setFechaEmision(entity.getFechaEmision());
        invoice.setSubtotal(entity.getSubtotal());
        invoice.setIva(entity.getIva());
        invoice.setTotal(entity.getTotal());
        invoice.setEstado(entity.getEstado());

        if (entity.getDetalles() != null) {
            List<InvoiceDetail> detalles = entity.getDetalles().stream().map(de -> {
                InvoiceDetail d = new InvoiceDetail();
                d.setId(de.getId());
                d.setProductoId(de.getProductoId());
                d.setProductoNombre(de.getProductoNombre());
                d.setCantidad(de.getCantidad());
                d.setPrecioUnitario(de.getPrecioUnitario());
                d.setSubtotal(de.getSubtotal());
                return d;
            }).collect(Collectors.toList());
            invoice.setDetalles(detalles);
        }
        return invoice;
    }
}
