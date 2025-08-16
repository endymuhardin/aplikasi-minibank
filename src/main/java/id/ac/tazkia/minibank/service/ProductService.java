package id.ac.tazkia.minibank.service;

import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    public List<Product> findActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }
    
    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }
    
    public Optional<Product> findByProductCode(String productCode) {
        return productRepository.findByProductCode(productCode);
    }
    
    public List<Product> findByProductType(Product.ProductType productType) {
        return productRepository.findByProductType(productType);
    }
    
    public List<Product> findWithFilters(Product.ProductType productType, String category, String searchTerm) {
        return productRepository.findActiveProductsWithFilters(productType, category, searchTerm);
    }
    
    public Page<Product> findWithFilters(Product.ProductType productType, String category, String searchTerm, Pageable pageable) {
        return productRepository.findActiveProductsWithFilters(productType, category, searchTerm, pageable);
    }
    
    public List<String> findDistinctCategories() {
        return productRepository.findDistinctActiveCategories();
    }
    
    @Transactional
    public Product save(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }
    
    @Transactional
    public Product update(Product product) {
        validateProduct(product);
        if (product.getId() == null) {
            throw new IllegalArgumentException("Product ID is required for update operation");
        }
        if (!productRepository.existsById(product.getId())) {
            throw new IllegalArgumentException("Product with ID " + product.getId() + " not found for update");
        }
        return productRepository.save(product);
    }
    
    @Transactional
    public void deleteById(UUID id) {
        productRepository.deleteById(id);
    }
    
    @Transactional
    public void softDelete(UUID id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            Product p = product.get();
            p.setIsActive(false);
            productRepository.save(p);
        }
    }
    
    public boolean existsByProductCode(String productCode) {
        return productRepository.existsByProductCode(productCode);
    }
    
    public boolean existsByProductCodeAndNotId(String productCode, UUID id) {
        Optional<Product> existing = productRepository.findByProductCode(productCode);
        return existing.isPresent() && !existing.get().getId().equals(id);
    }
    
    private void validateProduct(Product product) {
        if (product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Product code is required");
        }
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getProductType() == null) {
            throw new IllegalArgumentException("Product type is required");
        }
        if (product.getProfitSharingRatio() != null && 
            (product.getProfitSharingRatio().compareTo(java.math.BigDecimal.ZERO) < 0 || 
             product.getProfitSharingRatio().compareTo(java.math.BigDecimal.ONE) > 0)) {
            throw new IllegalArgumentException("Profit sharing ratio must be between 0 and 1");
        }
    }
}