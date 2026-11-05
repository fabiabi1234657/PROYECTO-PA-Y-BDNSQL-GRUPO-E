package com.facturacion.infrastructure.adapter.cassandra;

import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PersonCassandraRepository extends CassandraRepository<PersonEntity, String> {
    @AllowFiltering
    List<PersonEntity> findByCategoria(String categoria);
}
