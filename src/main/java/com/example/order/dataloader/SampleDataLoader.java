package com.example.order.dataloader;

import com.example.order.domain.ShoppingCart;
import com.example.order.repository.ShoppingCartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author mukibul
 * @since 24/08/19
 */
@Slf4j
@Component
public class SampleDataLoader implements CommandLineRunner {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;


    @Override
    public void run(String... args) throws Exception {

        ShoppingCart cart = new ShoppingCart();
        cart = shoppingCartRepository.save(cart);
        log.info("Cart id = {}",cart.getId());
    }
}
