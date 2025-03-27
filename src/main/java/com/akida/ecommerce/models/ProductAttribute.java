package com.akida.ecommerce.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "product_attributes")
public class ProductAttribute extends BasicEntity{
    @Id
    @SequenceGenerator(name="product_attributes_sequence",sequenceName ="appuser_sequence",allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "appuser_sequence")
    private Long id;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String value;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
