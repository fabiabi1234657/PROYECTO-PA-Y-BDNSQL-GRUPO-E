package com.facturacion.domain.port.in;

import com.facturacion.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface GetProductsUseCase {
    List<Product> getAllProducts();
    Optional<Product> getProductById(String id);
    Product saveProduct(Product product);
}
