package com.akida.ecommerce.DTOMapper;

import com.akida.ecommerce.DTO.AppUserDTO;
import com.akida.ecommerce.models.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AppUserDTOMapper implements Function<AppUser, AppUserDTO> {

    @Override
    public AppUserDTO apply(AppUser appUser) {
        return new AppUserDTO(
                appUser.getId(),
                appUser.getFirstName(),
                appUser.getLastName(),
                appUser.getEmail(),
                appUser.getTelNumber(),
                appUser.getAddress(),
                appUser.getAuthorities()
                        .stream().map(GrantedAuthority::getAuthority)
                        .toList(),

                appUser.isEnabled()
        );
    }
}