package com.akida.ecommerce.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "discounts")
public class Discount extends BasicEntity{
    @Id
    @SequenceGenerator(name="discount_sequence",sequenceName ="discount_sequence",allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "discount_sequence")
    private Long id;

    @Column(nullable = false)
    private Double percentage;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"discount"})
    private List<Product> products;
}