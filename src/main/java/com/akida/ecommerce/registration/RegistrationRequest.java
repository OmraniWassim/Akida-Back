package com.akida.ecommerce.registration;


import com.akida.ecommerce.Enumarators.AppUserRole;

public record RegistrationRequest(String firstName, String lastName, String email, AppUserRole appUserRole,
                                  String password) {
}
