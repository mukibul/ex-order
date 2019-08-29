package com.example.order;

import com.example.order.domain.CartLine;
import com.example.order.domain.Product;
import com.example.order.domain.ShoppingCart;
import com.example.order.domain.messaging.ProductEventSink;
import com.example.order.repository.ProductRepository;
import com.example.order.repository.ShoppingCartRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.hypermedia.HypermediaDocumentation;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.restassured3.RestDocumentationFilter;
import org.springframework.restdocs.restassured3.operation.preprocess.RestAssuredPreprocessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.halLinks;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;

/**
 * @author mukibul
 * @since 24/08/19
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(value = {"eureka.client.enabled=false","zipkin.client.enable=false"},webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OrderApiDocumentation {
    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    @Getter
    @Setter
    private RestOperations restTemplate	= new RestTemplate();

//    @Value("http://localhost:${local.server.port}")
//    protected String host;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ProductRepository productRepository;

    @LocalServerPort
    private int port;


    @Autowired
    private ProductEventSink pipe;

    @Autowired
    private MessageCollector messageCollector;

    private RestDocumentationFilter documentationFilter;

    private RequestSpecification spec;



   @Before
    public void setUp() {

        this.documentationFilter = document("{method-name}",
                Preprocessors.preprocessRequest(RestAssuredPreprocessors.modifyUris()
                        .host("172.27.80.77")
                        .removePort()));

        this.spec = new RequestSpecBuilder()
                .addFilter(documentationConfiguration(this.restDocumentation))
                .addFilter(this.documentationFilter).build();
    }


    //Ignore self and profile links in documentation
    public static LinksSnippet links(LinkDescriptor... descriptors) {
        return HypermediaDocumentation.links(halLinks(),descriptors).and(linkWithRel("self").ignored().optional(),
                linkWithRel("curies").ignored().optional(),linkWithRel("profile").ignored().optional());
    }

    //Ignore all _links fields in documentation
    public static ResponseFieldsSnippet responseFields(FieldDescriptor... fieldDescriptors){
        List<FieldDescriptor> filteredDescriptors = new ArrayList<>(0);
        for(FieldDescriptor descriptor: fieldDescriptors){
            if(descriptor.getPath().contains("_links")){
                descriptor.ignored();
            }
            filteredDescriptors.add(descriptor);
        }

        return PayloadDocumentation.responseFields(filteredDescriptors);
    }


    @Test
    public void getShoppingCart() {

        RestAssured.given(this.spec)
                .accept("application/json")
                .filter(this.documentationFilter.document(
                        responseFields(
                                fieldWithPath("cartLines[]").description("Products added to cart"),
                                fieldWithPath("cartLines[].quantity").description("Products quantity"),
                                fieldWithPath("cartLines[].price").description("Products price"),
                                fieldWithPath("cartLines[].subTotal").description("Subtotal of the products i.e. quanity * price"),
                                fieldWithPath("cartLines[]._links.product.href").description("Link to the product resource"),
                                fieldWithPath("_links.shoppingCart.href").description("<<shopping-cart-links,Links>> to self"),
                                fieldWithPath("_links.self.href").description("<<shopping-cart-links,Links>> to self")
                        )
                ))
                .when().port(this.port).get("/shoppingCarts/{cartId}",1)
                .then()
                .assertThat().statusCode(200)
                .log().body();
    }

    @Test
    public void addProductQuantityInShoppingCart() {
        Product product = new Product(2L,"Test obj",2000.0);
        product = productRepository.save(product);
        ShoppingCart cart = shoppingCartRepository.findById(1L).orElse(null);

        CartLine cartLine =cartLine = new CartLine(product, 1);
        cart.getCartLines().add(cartLine);
        shoppingCartRepository.save(cart);
        RestAssured.given(this.spec)
                .accept("application/json")
                .filter(this.documentationFilter.document(
                        pathParameters(parameterWithName("cartId").description("Shopping cart id"),
                                parameterWithName("productId").description("Product id")
                        ),
                        responseFields(
                                fieldWithPath("cartLines[]").description("Products added to cart"),
                                fieldWithPath("cartLines[].quantity").description("Products quantity"),
                                fieldWithPath("cartLines[].price").description("Products price"),
                                fieldWithPath("cartLines[].subTotal").description("Subtotal of the products i.e. quanity * price"),
                                fieldWithPath("cartLines[]._links.product.href").description("Link to the product resource"),
                                fieldWithPath("_links.self.href").description("<<shopping-cart-links,Links>> to self")
                        )
                ))
                .when().port(this.port).put("/shoppingCarts/{cartId}/addQuantity/{productId}",cart.getId(),product.getProductId())
                .then()
                .assertThat().statusCode(200)
                .log().body();
    }

    @Test
    public void reduceProductQuantityInShoppingCart() {
        Product product = new Product(3L,"Test obj",2000.0);
        product = productRepository.save(product);
        //ProductAddToCartEvent.Product product1 = new ProductAddToCartEvent.Product(product.getId(),"Test obj","2000.0");
        ShoppingCart cart = shoppingCartRepository.findById(1L).orElse(null);

        CartLine cartLine =cartLine = new CartLine(product, 2);
        cart.getCartLines().add(cartLine);

        shoppingCartRepository.save(cart);

//        ProductAddToCartEvent productAddToCartEvent =new ProductAddToCartEvent(product1,1L);
//        boolean send = pipe.productEventsSubscriberChannel()
//                .send(MessageBuilder.withPayload(productAddToCartEvent)
//                        .build());

        RestAssured.given(this.spec)
                .accept("application/json")
                .filter(this.documentationFilter.document(
                        pathParameters(parameterWithName("cartId").description("Shopping cart id"),
                                parameterWithName("productId").description("Product id")
                        ),
                        responseFields(
                                fieldWithPath("cartLines[]").description("Products added to cart"),
                                fieldWithPath("cartLines[].quantity").description("Products quantity"),
                                fieldWithPath("cartLines[].price").description("Products price"),
                                fieldWithPath("cartLines[].subTotal").description("Subtotal of the products i.e. quanity * price"),
                                fieldWithPath("cartLines[]._links.product.href").description("Link to the product resource"),
                                fieldWithPath("_links.self.href").description("<<shopping-cart-links,Links>> to self")
                        )
                ))
                .when().port(this.port).put("/shoppingCarts/{cartId}/reduceQuantity/{productId}",cart.getId(),product.getProductId())
                .then()
                .assertThat().statusCode(200)
                .log().body();
    }

    @Test
    public void updateProductQuantityInShoppingCart() throws JsonProcessingException {
        Product product = new Product(4L,"Test obj",2000.0);
        product = productRepository.save(product);
        //ProductAddToCartEvent.Product product1 = new ProductAddToCartEvent.Product(product.getId(),"Test obj","2000.0");
        ShoppingCart cart = shoppingCartRepository.findById(1L).orElse(null);

        CartLine cartLine =cartLine = new CartLine(product, 2);
        cart.getCartLines().add(cartLine);
        shoppingCartRepository.save(cart);

        Map<String,Object> productQuantityMap = new HashMap<>();
        productQuantityMap.put("quantity",5);


        RestAssured.given(this.spec)
                .accept("application/json")
                .filter(this.documentationFilter.document(
                        pathParameters(parameterWithName("cartId").description("Shopping cart id"),
                                parameterWithName("productId").description("Product id")
                                ),
                        responseFields(
                                fieldWithPath("cartLines[]").description("Products added to cart"),
                                fieldWithPath("cartLines[].quantity").description("Products quantity"),
                                fieldWithPath("cartLines[].price").description("Products price"),
                                fieldWithPath("cartLines[].subTotal").description("Subtotal of the products i.e. quanity * price"),
                                fieldWithPath("cartLines[]._links.product.href").description("Link to the product resource"),
                                fieldWithPath("_links.self.href").description("<<shopping-cart-links,Links>> to self")
                                //fieldWithPath("_links.shoppingCart.href").description("<<shopping-cart-links,Links>> to the shopping cart")
                        )
                ))
                .content(this.objectMapper.writeValueAsString(productQuantityMap)).contentType("application/json")
                .when().port(this.port).put("/shoppingCarts/{cartId}/updateQuantity/{productId}",cart.getId(),product.getProductId())
                .then()
                .assertThat().statusCode(200)
                .log().body();
    }

}
