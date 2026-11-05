package com.facturacion.domain.port.out;

import com.facturacion.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    List<Product> findAll();
    Optional<Product> findById(String id);
    Product save(Product product);
}
