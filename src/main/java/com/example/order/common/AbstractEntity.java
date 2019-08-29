package com.example.order.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.Identifiable;

import javax.persistence.*;

@Getter
@ToString
@MappedSuperclass
@EqualsAndHashCode
public class AbstractEntity implements Identifiable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private final  Long id;

    @Version
    private Long version;

    protected AbstractEntity() {
        this.id = null;
    }
}
