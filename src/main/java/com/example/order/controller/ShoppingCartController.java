package com.example.order.controller;

import com.example.order.domain.CartLine;
import com.example.order.domain.ShoppingCart;
import com.example.order.repository.ShoppingCartRepository;
import com.example.order.service.ShoppingCartService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author mukibul
 * @since 26/08/19
 */

@Slf4j
@BasePathAwareController
public class ShoppingCartController {

    @Autowired
    protected Repositories repositories;


    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * This adds quantity of a product in shopping cart
     *
     * @param shoppingCart
     * @param productId
     * @return
     */
    @PutMapping("/shoppingCarts/{cart_id}/addQuantity/{product_id}")
    public ResponseEntity<?> addQuantity(@PathVariable("cart_id") ShoppingCart shoppingCart,
                                         @PathVariable("product_id") Long productId
                                         ){

        shoppingCart = shoppingCartService.incrementQuantity(shoppingCart, productId);
        PersistentEntityResource shoppingCartResource = PersistentEntityResource.build(shoppingCart,repositories.getPersistentEntity(ShoppingCart.class)).build();
        return new ResponseEntity<Object>(shoppingCartResource,HttpStatus.OK);

    }

    /**
     *  This reduces a quantity of a product in shopping cart
     *
     * @param shoppingCart
     * @param productId
     * @return
     */
    @PutMapping("/shoppingCarts/{cart_id}/reduceQuantity/{product_id}")
    public ResponseEntity<?> reduceQuantity(@PathVariable("cart_id") ShoppingCart shoppingCart,
                                         @PathVariable("product_id") Long productId
                                        ){

        shoppingCart = shoppingCartService.decrementQuantity(shoppingCart, productId);
        PersistentEntityResource shoppingCartResource = PersistentEntityResource.build(shoppingCart,repositories.getPersistentEntity(ShoppingCart.class)).build();
        return new ResponseEntity<Object>(shoppingCartResource,HttpStatus.OK);

    }

    /**
     *  This updates a quantity of a product in a shopping cart
     *
     * @param shoppingCart
     * @param productId
     * @param productQuantity
     * @return
     */

    @PutMapping("/shoppingCarts/{cart_id}/updateQuantity/{product_id}")
    public ResponseEntity<?> updateQuantity(@PathVariable("cart_id") ShoppingCart shoppingCart,
                                            @PathVariable("product_id") Long productId,
                                            @RequestBody ProductQuantity productQuantity){

        shoppingCart = shoppingCartService.updateQuantity(shoppingCart, productId,productQuantity.getQuantity());
        PersistentEntityResource shoppingCartResource = PersistentEntityResource.build(shoppingCart,repositories.getPersistentEntity(ShoppingCart.class)).build();
        return new ResponseEntity<Object>(shoppingCartResource,HttpStatus.OK);

    }
    @Data
    public static class ProductQuantity {
        private Integer quantity;
    }



}
