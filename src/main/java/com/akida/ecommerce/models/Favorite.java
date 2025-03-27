package com.akida.ecommerce.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "favorites")
public class Favorite extends BasicEntity{
    @Id
    @SequenceGenerator(name="favorites_sequence",sequenceName ="favorites_sequence",allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "favorites_sequence")
    private Long id;

    @Column(nullable = false)
    private LocalDate addedDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}