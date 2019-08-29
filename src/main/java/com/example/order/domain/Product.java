package com.example.order.domain;

import com.example.order.common.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.money.MonetaryAmount;
import javax.persistence.Convert;
import javax.persistence.Entity;

/**
 * @author mukibul
 * @since 24/08/19
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends AbstractEntity {

    private Long productId;

    private String name;

    private Double price;

}
