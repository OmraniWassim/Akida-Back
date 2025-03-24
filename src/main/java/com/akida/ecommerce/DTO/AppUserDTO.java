package com.akida.ecommerce.DTO;

import java.util.List;

public record AppUserDTO (
        Long id,
        String FirstName,
        String LastName,
        String email,
        List<String> appUserRoles,
        Boolean enabled

){

}
