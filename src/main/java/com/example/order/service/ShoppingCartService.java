package com.example.order.service;

import com.example.order.domain.ShoppingCart;
import com.example.order.repository.ShoppingCartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author mukibul
 * @since 27/08/19
 */
@Slf4j
@Service("shoppingCartService")
public class ShoppingCartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    public ShoppingCart incrementQuantity(ShoppingCart cart, Long productId){

        cart.getCartLines().stream().forEach(cartLine -> {
            if(cartLine.getProduct().getProductId().equals(productId)){
                cartLine.incrementQuantity();
            }
        });

        return shoppingCartRepository.save(cart);
    }

    public ShoppingCart decrementQuantity(ShoppingCart cart, Long productId){

        cart.getCartLines().stream().forEach(cartLine -> {
            if(cartLine.getProduct().getProductId().equals(productId)){
                cartLine.decrementQuantity();
            }
        });

        return shoppingCartRepository.save(cart);
    }

    public ShoppingCart updateQuantity(ShoppingCart cart, Long productId,Integer quantity){

        cart.getCartLines().stream().forEach(cartLine -> {
            if(cartLine.getProduct().getProductId().equals(productId)){
                cartLine.updateQuantity(quantity);
            }
        });

        return shoppingCartRepository.save(cart);
    }
}
