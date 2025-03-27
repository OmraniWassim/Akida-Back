package com.akida.ecommerce.services;

import com.akida.ecommerce.DTO.AppUserDTO;
import com.akida.ecommerce.Enumarators.AppUserRole;
import com.akida.ecommerce.registration.LoginRequest;
import com.akida.ecommerce.registration.PwdRequest;
import com.akida.ecommerce.registration.RegistrationRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface RegistrationService {
    String register(RegistrationRequest request);

    List<AppUserDTO> findAllAppUsers();

    void updateAppUser(RegistrationRequest request, Long id);

    Optional<AppUserDTO> findById(Long id);

    void deleteAppUser(Long id);

    List<AppUserDTO> findByRole(AppUserRole role);

    HashMap<String,String> login(@RequestBody LoginRequest loginRequest);

    HashMap<String,String> ChangePWD(Long id, PwdRequest newPWD);

    @Transactional
    String confirmToken(String token);
}
