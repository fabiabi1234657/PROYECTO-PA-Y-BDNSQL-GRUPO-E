package com.facturacion.infrastructure.adapter.mysql;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, Long> {
    @Override
    @EntityGraph(attributePaths = "detalles")
    List<InvoiceEntity> findAll();
}
