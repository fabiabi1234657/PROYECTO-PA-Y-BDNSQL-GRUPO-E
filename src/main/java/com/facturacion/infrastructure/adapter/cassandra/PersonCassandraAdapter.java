package com.facturacion.infrastructure.adapter.cassandra;

import com.facturacion.domain.model.Person;
import com.facturacion.domain.port.out.PersonRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PersonCassandraAdapter implements PersonRepositoryPort {

    private final PersonCassandraRepository cassandraRepository;

    public PersonCassandraAdapter(PersonCassandraRepository cassandraRepository) {
        this.cassandraRepository = cassandraRepository;
    }

    @Override
    public List<Person> findAll() {
        return cassandraRepository.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Person> findByCategoria(String categoria) {
        return cassandraRepository.findByCategoria(categoria).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Person> findByDocumento(String documento) {
        return cassandraRepository.findById(documento).map(this::toModel);
    }

    @Override
    public Person save(Person person) {
        return toModel(cassandraRepository.save(toEntity(person)));
    }

    private Person toModel(PersonEntity e) {
        return new Person(e.getDocumento(), e.getNombre(), e.getCorreo(),
                e.getTelefono(), e.getCiudad(), e.getCategoria());
    }

    private PersonEntity toEntity(Person p) {
        PersonEntity e = new PersonEntity();
        e.setDocumento(p.getDocumento());
        e.setNombre(p.getNombre());
        e.setCorreo(p.getCorreo());
        e.setTelefono(p.getTelefono());
        e.setCiudad(p.getCiudad());
        e.setCategoria(p.getCategoria());
        return e;
    }
}
