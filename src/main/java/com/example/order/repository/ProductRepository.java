package com.example.order.repository;

import com.example.order.domain.Product;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author mukibul
 * @since 24/08/19
 */
public interface ProductRepository extends PagingAndSortingRepository<Product,Long> {

    Product findByProductId(@Param("product_id") Long product_id);
}
