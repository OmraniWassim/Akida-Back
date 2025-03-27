package com.akida.ecommerce.models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class BasicEntity {
    @Column(name="cuticre")
    private String cuticre;
    @Column(name="cutimod")
    private String cutimod;

    @Column(name="datecre")
    private LocalDateTime datecre;
    @Column(name="datemod")
    private LocalDateTime datemod;
}
