package com.facturacion.application;

import com.facturacion.domain.model.Person;
import com.facturacion.domain.port.in.RegisterPersonUseCase;
import com.facturacion.domain.port.out.PersonRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonService implements RegisterPersonUseCase {

    private final PersonRepositoryPort personRepository;

    public PersonService(PersonRepositoryPort personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    @Override
    public List<Person> getPersonsByCategoria(String categoria) {
        return personRepository.findByCategoria(categoria);
    }

    @Override
    public Optional<Person> getPersonByDocumento(String documento) {
        return personRepository.findByDocumento(documento);
    }

    @Override
    public Person savePerson(Person person) {
        return personRepository.save(person);
    }
}
