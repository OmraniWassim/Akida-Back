package com.akida.ecommerce.registration;


import com.akida.ecommerce.Enumarators.AppUserRole;

public record RegistrationRequest(String first_name, String last_name, String email, AppUserRole app_user_role,
                                  String password) {
}
