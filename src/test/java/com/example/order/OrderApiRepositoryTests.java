package com.example.order;

import com.example.event.ProductAddToCartEvent;
import com.example.order.domain.Product;
import com.example.order.domain.ShoppingCart;
import com.example.order.domain.messaging.ProductEventSink;
import com.example.order.repository.ProductRepository;
import com.example.order.repository.ShoppingCartRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessagingException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mukibul
 * @since 27/08/19
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class OrderApiRepositoryTests {
    @Rule
    public ExpectedException expectedException= ExpectedException.none();

    private Product product;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductEventSink pipe;

    @Autowired
    private MessageCollector messageCollector;

    @Before
    public void setUp() {

        product = new Product(1L,"Test obj",2000.0);
        product = productRepository.save(product);

    }

    @Test
    public void createShoppingCart() {
        ShoppingCart cart = new ShoppingCart();
        cart = shoppingCartRepository.save(cart);

        Assert.notNull(cart, "Cart created");
        Assert.notNull(cart.getId(), "Cart id presend");


    }

    @Test
    public void testProductAddToCartEventWhenProductIsPresent(){
        ProductAddToCartEvent.Product product1 = new ProductAddToCartEvent.Product(product.getProductId(),"Test obj","2000.0");

        ProductAddToCartEvent productAddToCartEvent =new ProductAddToCartEvent(product1,1L);
        boolean send = pipe.productEventsSubscriberChannel()
                .send(MessageBuilder.withPayload(productAddToCartEvent)
                        .build());
        ShoppingCart cart = shoppingCartRepository.findById(1L).orElse(null);
        final Integer[] quantity = {null};
        final Double[] subTotal = {null};
        cart.getCartLines().stream().forEach(cartLine -> {
            quantity[0] = cartLine.getQuantity();
            subTotal[0] = cartLine.getSubTotal();

        });
        assertEquals(1,quantity[0].intValue());
        assertEquals(2000.0,subTotal[0].doubleValue(),0);
        Assert.notNull(cart,"Cart is present");
        assertEquals(1,cart.getCartLines().size());

        assertTrue(send);

        send = pipe.productEventsSubscriberChannel()
                .send(MessageBuilder.withPayload(productAddToCartEvent)
                        .build());

        cart = shoppingCartRepository.findById(1L).orElse(null);
        cart.getCartLines().stream().forEach(cartLine -> {
            quantity[0] = cartLine.getQuantity();
            subTotal[0] = cartLine.getSubTotal();
        });
        assertEquals(2,quantity[0].intValue());
        assertEquals(4000.0,subTotal[0].doubleValue(),0);
    }
    @Test
    public void testProductAddToCartEventProductIsNotPresent(){
        expectedException.expect(MessagingException.class);
        expectedException.expectMessage("Product cannot be null");
        ProductAddToCartEvent.Product product1 = new ProductAddToCartEvent.Product(5L,"Test obj","2000.0");

        ProductAddToCartEvent productAddToCartEvent =new ProductAddToCartEvent(product1,1L);
        boolean send = pipe.productEventsSubscriberChannel()
                .send(MessageBuilder.withPayload(productAddToCartEvent)
                        .build());


    }

}
