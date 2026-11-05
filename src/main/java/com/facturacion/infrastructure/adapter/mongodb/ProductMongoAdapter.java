package com.facturacion.infrastructure.adapter.mongodb;

import com.facturacion.domain.model.Product;
import com.facturacion.domain.model.Supplier;
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
        Supplier supplier = null;
        if (doc.getProveedor() != null) {
            supplier = new Supplier(
                doc.getProveedor().getNombre(),
                doc.getProveedor().getContacto(),
                doc.getProveedor().getTelefono()
            );
        }
        return new Product(doc.getId(), doc.getNombre(), doc.getDescripcion(),
                doc.getCosto(), doc.getPrecioVenta(), supplier, doc.getImageUrls());
    }

    private ProductDocument toDocument(Product product) {
        ProductDocument doc = new ProductDocument();
        doc.setId(product.getId());
        doc.setNombre(product.getNombre());
        doc.setDescripcion(product.getDescripcion());
        doc.setCosto(product.getCosto());
        doc.setPrecioVenta(product.getPrecioVenta());
        doc.setImageUrls(product.getImageUrls());
        if (product.getProveedor() != null) {
            ProductDocument.SupplierEmbedded se = new ProductDocument.SupplierEmbedded();
            se.setNombre(product.getProveedor().getNombre());
            se.setContacto(product.getProveedor().getContacto());
            se.setTelefono(product.getProveedor().getTelefono());
            doc.setProveedor(se);
        }
        return doc;
    }
}
