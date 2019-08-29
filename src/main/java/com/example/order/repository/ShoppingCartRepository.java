package com.example.order.repository;

import com.example.order.domain.ShoppingCart;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author mukibul
 * @since 24/08/19
 */
public interface ShoppingCartRepository extends PagingAndSortingRepository<ShoppingCart,Long> {
}
