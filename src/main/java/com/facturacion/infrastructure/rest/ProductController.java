package com.facturacion.infrastructure.rest;

import com.facturacion.domain.model.Product;
import com.facturacion.domain.port.in.GetProductsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final GetProductsUseCase getProductsUseCase;

    public ProductController(GetProductsUseCase getProductsUseCase) {
        this.getProductsUseCase = getProductsUseCase;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return getProductsUseCase.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return getProductsUseCase.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return getProductsUseCase.saveProduct(product);
    }
}
