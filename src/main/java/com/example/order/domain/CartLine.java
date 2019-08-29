package com.example.order.domain;

import com.example.order.common.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.javamoney.moneta.Money;
import org.springframework.util.Assert;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 *
 * @author mukibul
 * @since 24/08/19
 *
 */
@Entity
@Getter
@EqualsAndHashCode(exclude = {"quantity"})
@ToString
public class CartLine extends AbstractEntity {

    @ManyToOne
    private Product product;
    private Integer quantity;

    private final Double price;

    protected CartLine(){
        this.product=null;
        this.quantity=null;
        this.price = null;
    }

    public CartLine(@JsonProperty("product") Product product, @JsonProperty("quantity") Integer quantity){
        Assert.notNull(product, "Product cannot be null");
        Assert.notNull(quantity);

        this.product=product;
        this.quantity=quantity;
        this.price = product.getPrice() == null? 0.00  : product.getPrice();

    }



    public void incrementQuantity(){
        this.quantity = this.quantity + 1;
    }

    public void updateQuantity(Integer quantity){
        if(quantity > 0)
            this.quantity = quantity;
    }

    public void decrementQuantity(){
        if(this.quantity - 1 > 0)
            this.quantity = this.quantity - 1;
    }

    public Double getSubTotal(){
        return price == null? 0.00 : price.doubleValue()*quantity;
    }
}
