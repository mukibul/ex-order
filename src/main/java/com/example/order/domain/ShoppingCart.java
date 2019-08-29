package com.example.order.domain;

import com.example.order.common.AbstractEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mukibul
 * @since 23/08/19
 */

@Entity
@Getter
@NoArgsConstructor
public class ShoppingCart extends AbstractEntity {

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true,fetch = FetchType.EAGER)
    private Set<CartLine> cartLines = new HashSet<>(0);
}
