package com.facturacion.infrastructure.adapter.mongodb;

import com.facturacion.domain.model.Product;
import com.facturacion.domain.port.out.ProductRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductMongoAdapter implements ProductRepositoryPort {

    private final ProductMongoRepository mongoRepository;

    public ProductMongoAdapter(ProductMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public List<Product> findAll() {
        return mongoRepository.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Product> findById(String id) {
        return mongoRepository.findById(id).map(this::toModel);
    }

    @Override
    public Product save(Product product) {
        ProductDocument doc = toDocument(product);
        return toModel(mongoRepository.save(doc));
    }

    private Product toModel(ProductDocument doc) {
        return new Product(doc.getId(), doc.getNombre(), doc.getDescripcion(),
                doc.getCosto(), doc.getPrecioVenta(), doc.getProveedor(), doc.getImageUrls());
    }

    private ProductDocument toDocument(Product product) {
        ProductDocument doc = new ProductDocument();
        doc.setId(product.getId());
        doc.setNombre(product.getNombre());
        doc.setDescripcion(product.getDescripcion());
        doc.setCosto(product.getCosto());
        doc.setPrecioVenta(product.getPrecioVenta());
        doc.setProveedor(product.getProveedor());
        doc.setImageUrls(product.getImageUrls());
        return doc;
    }
}
