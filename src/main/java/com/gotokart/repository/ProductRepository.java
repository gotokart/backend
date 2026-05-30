package com.gotokart.repository;

import com.gotokart.model.Category;
import com.gotokart.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByNameIgnoreCase(String name);

    long countByCategory(Category category);

    // Used by the admin dashboard's "low stock alerts" widget.
    List<Product> findByStockLessThanEqualOrderByStockAsc(int threshold);

    @Query("SELECT p FROM Product p WHERE p.imageUrl IS NULL OR TRIM(p.imageUrl) = ''")
    List<Product> findWithoutImage();

    /** Missing images plus rows pointing at private S3 URLs that browsers cannot load. */
    @Query("""
            SELECT p FROM Product p
            WHERE p.imageUrl IS NULL OR TRIM(p.imageUrl) = ''
               OR LOWER(p.imageUrl) LIKE '%.s3.%amazonaws.com/%'
            """)
    List<Product> findNeedingImageRefresh();
}
