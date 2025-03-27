package com.akida.ecommerce.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "ratings")
public class Rating extends BasicEntity{
    @Id
    @SequenceGenerator(name="ratings_sequence",sequenceName ="ratings_sequence",allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "ratings_sequence")
    private Long id;

    @Column(nullable = false)
    private int ratingValue;

    private String comment;

    @Column(nullable = false)
    private LocalDate ratingDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}