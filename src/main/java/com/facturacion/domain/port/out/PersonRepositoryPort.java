package com.facturacion.domain.port.out;

import com.facturacion.domain.model.Person;
import java.util.List;
import java.util.Optional;

public interface PersonRepositoryPort {
    List<Person> findAll();
    List<Person> findByCategoria(String categoria);
    Optional<Person> findByDocumento(String documento);
    Person save(Person person);
}
