package com.akida.ecommerce.serviceimpl;



import com.akida.ecommerce.models.AppUser;
import com.akida.ecommerce.registration.token.ConfirmationToken;
import com.akida.ecommerce.registration.token.ConfirmationTokenService;
import com.akida.ecommerce.repository.AppUserRepository;
import com.akida.ecommerce.services.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserServiceImpl implements AppUserService {
    private final static String USER_NOT_FOUND_MSG="Utilisateur avec email %s exist pas";
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG,email)));
    }
    @Override
    public String signUpUser(AppUser appUser){
        boolean userExists =appUserRepository.findByEmail(appUser.getEmail()).isPresent();
        if(userExists){
            throw new IllegalStateException("email already exists");
        }
        String encodedPassword=bCryptPasswordEncoder.encode(appUser.getPassword());
        appUser.setPassword(encodedPassword);
        appUserRepository.save(appUser);
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser


        );
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        return token;
    }


    @Override
    public void enableAppUser(String email) {
        appUserRepository.enableAppUser(email);
    }
}
