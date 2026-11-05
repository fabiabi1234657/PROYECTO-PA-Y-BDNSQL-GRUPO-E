package com.facturacion.infrastructure.rest;

import com.facturacion.domain.model.Person;
import com.facturacion.domain.port.in.RegisterPersonUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "*")
public class PersonController {

    private final RegisterPersonUseCase registerPersonUseCase;

    public PersonController(RegisterPersonUseCase registerPersonUseCase) {
        this.registerPersonUseCase = registerPersonUseCase;
    }

    @GetMapping
    public List<Person> getAllPersons() {
        return registerPersonUseCase.getAllPersons();
    }

    @GetMapping("/categoria/{categoria}")
    public List<Person> getByCategoria(@PathVariable String categoria) {
        return registerPersonUseCase.getPersonsByCategoria(categoria.toUpperCase());
    }

    @GetMapping("/{documento}")
    public ResponseEntity<Person> getByDocumento(@PathVariable String documento) {
        return registerPersonUseCase.getPersonByDocumento(documento)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Person createPerson(@RequestBody Person person) {
        return registerPersonUseCase.savePerson(person);
    }
}
