package com.akida.ecommerce.controller;


import com.akida.ecommerce.DTO.AppUserDTO;
import com.akida.ecommerce.Enumarators.AppUserRole;
import com.akida.ecommerce.registration.LoginRequest;
import com.akida.ecommerce.registration.PwdRequest;
import com.akida.ecommerce.registration.RegistrationRequest;
import com.akida.ecommerce.repository.AppUserRepository;
import com.akida.ecommerce.serviceimpl.RegistrationServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("api/v1/registration")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RegistrationController {
    private RegistrationServiceImpl registrationServiceImpl;
    private final AppUserRepository appUserRepository;



    @GetMapping("/all")
    //@PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AppUserDTO>> getAllAppUsers () {
        List<AppUserDTO> appUsers= registrationServiceImpl.findAllAppUsers();
        return new ResponseEntity<>(appUsers, HttpStatus.OK);
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest request){
        if(appUserRepository.findByEmail(request.email()).isPresent()) {
            return new ResponseEntity<>( HttpStatus.BAD_REQUEST);

        }else{
            return new ResponseEntity<>(registrationServiceImpl.register(request), HttpStatus.OK);
        }

    }

    @PostMapping("/login")
    public ResponseEntity<HashMap<String,String>> login(@RequestBody LoginRequest loginRequest) {

        return new ResponseEntity<>(registrationServiceImpl.login(loginRequest), HttpStatus.OK);
    }
    @GetMapping("/find/{id}")
    public ResponseEntity<Optional<AppUserDTO>> getAppUserById (@PathVariable("id") Long id) {
        Optional<AppUserDTO> appUser = registrationServiceImpl.findById(id);
        return new ResponseEntity<>(appUser, HttpStatus.OK);
    }
    @GetMapping("/all/{role}")
    public ResponseEntity<List<AppUserDTO>> getAppUsersByRole(@PathVariable("role") AppUserRole role) {
        List<AppUserDTO> appUserDTOs = registrationServiceImpl.findByRole(role);
        return new ResponseEntity<>(appUserDTOs, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAppUser(@PathVariable("id") Long id,@RequestBody RegistrationRequest request) {
        registrationServiceImpl.updateAppUser(request,id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAppUser(@PathVariable("id") Long id) {
        registrationServiceImpl.deleteAppUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/updatePWD/{id}")
    public ResponseEntity<HashMap<String,String>> updateAppUser(@PathVariable("id") Long id,@RequestBody PwdRequest newPWD) {

        return new ResponseEntity<>(registrationServiceImpl.ChangePWD(id,newPWD),HttpStatus.OK);
    }


    @GetMapping(path = "confirm")
    public String confirm(@RequestParam("token") String token) {
        return registrationServiceImpl.confirmToken(token);
    }



}
