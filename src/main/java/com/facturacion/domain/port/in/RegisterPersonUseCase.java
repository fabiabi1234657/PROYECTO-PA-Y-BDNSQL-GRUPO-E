package com.facturacion.domain.port.in;

import com.facturacion.domain.model.Person;
import java.util.List;
import java.util.Optional;

public interface RegisterPersonUseCase {
    List<Person> getAllPersons();
    List<Person> getPersonsByCategoria(String categoria);
    Optional<Person> getPersonByDocumento(String documento);
    Person savePerson(Person person);
}
