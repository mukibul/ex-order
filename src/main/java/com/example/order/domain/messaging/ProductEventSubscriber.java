package com.example.order.domain.messaging;

import com.example.event.ProductAddToCartEvent;
import com.example.event.ProductAddedOrUpdated;
import com.example.order.domain.CartLine;
import com.example.order.domain.Product;
import com.example.order.domain.ShoppingCart;
import com.example.order.repository.ProductRepository;
import com.example.order.repository.ShoppingCartRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author mukibul
 * @since 24/08/19
 */

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class ProductEventSubscriber {

    private static final String ORIGINAL_QUEUE = "productAdded.exchange.orderProducts";

    private static final String DLQ = ORIGINAL_QUEUE + ".dlq";

    private static final String PARKING_LOT = ORIGINAL_QUEUE + ".parkingLot";

    private static final String X_RETRIES_HEADER = "x-retries";

    private static final String DELAY_EXCHANGE = "dlqReRouter";



    @Autowired
    private RabbitTemplate rabbitTemplate;

    @NonNull
    private ShoppingCartRepository shoppingCartRepository;

    @NonNull
    private ProductRepository productRepository;

    @StreamListener(ProductEventSink.PRODUCT_SUBSCRIBER_EVENTS)
    public void on(ProductAddToCartEvent productAddToCartEvent)  {

            ProductAddToCartEvent.Product product = productAddToCartEvent.getProduct();
            log.info("Product to be added to cart = {}", product.getName());
            log.info("Product price = {}", product.getPrice());

            Product product1 = productRepository.findByProductId(product.getId());
           // throw new IllegalArgumentException();
            ShoppingCart cart = shoppingCartRepository.findById(productAddToCartEvent.getCartId()).orElse(null);
            if (cart != null) {
                final CartLine[] existingCartLine = {null};
                cart.getCartLines().stream().forEach(cartLine -> {
                    if(cartLine.getProduct().getProductId()==product.getId()){
                        existingCartLine[0] =cartLine;
                        cartLine.incrementQuantity();
                    }
                });
                CartLine cartLine = null;
                    if(existingCartLine[0]==null){
                        cartLine = new CartLine(product1, 1);
                        cart.getCartLines().add(cartLine);
                    }

                shoppingCartRepository.save(cart);
            }

    }

    @StreamListener(ProductEventSink.PRODUCT_ADDED_SUBSCRIBER_EVENTS)
    public void on(ProductAddedOrUpdated productAddedOrUpdated) throws Exception {

        log.info("Product received = {}", productAddedOrUpdated.getProductName());
        Product product = productRepository.findByProductId(productAddedOrUpdated.getProductId());
      //  throw new Exception("Testing for dlq");
        if(product==null) {
            product = new Product(productAddedOrUpdated.getProductId(), productAddedOrUpdated.getProductName(), productAddedOrUpdated.getProductPrice());
        } else {
            product.setName(productAddedOrUpdated.getProductName());
            product.setPrice(productAddedOrUpdated.getProductPrice());
        }
        productRepository.save(product);

    }

    @RabbitListener(queues = DLQ)
    public void processFailedMessage(Message failedMessage){
        log.info("Processing Failed message, {}",failedMessage.toString());

        Map<String, Object> headers = failedMessage.getMessageProperties().getHeaders();
        Integer retriesHeader = (Integer) headers.get(X_RETRIES_HEADER);
        if (retriesHeader == null) {
            retriesHeader = Integer.valueOf(0);
        }
        if (retriesHeader < 3) {
            headers.put(X_RETRIES_HEADER, retriesHeader + 1);
            headers.put("x-delay", 10000 * retriesHeader);
            this.rabbitTemplate.send(DELAY_EXCHANGE, ORIGINAL_QUEUE, failedMessage);
        }
        else {
            this.rabbitTemplate.send(PARKING_LOT, failedMessage);
        }
    }

    @Bean
    public DirectExchange delayExchange() {
        DirectExchange exchange = new DirectExchange(DELAY_EXCHANGE);
        exchange.setDelayed(true);
        return exchange;
    }

    @Bean
    public Binding bindOriginalToDelay() {
        return BindingBuilder.bind(new Queue(ORIGINAL_QUEUE)).to(delayExchange()).with(ORIGINAL_QUEUE);
    }

    @Bean
    public Queue parkingLot() {
        return new Queue(PARKING_LOT);
    }
}
