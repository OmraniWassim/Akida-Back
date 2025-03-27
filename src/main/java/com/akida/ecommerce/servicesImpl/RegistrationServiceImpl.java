package com.akida.ecommerce.servicesImpl;


import com.akida.ecommerce.registration.EmailValidator;
import com.akida.ecommerce.registration.LoginRequest;
import com.akida.ecommerce.registration.PwdRequest;
import com.akida.ecommerce.registration.RegistrationRequest;
import com.akida.ecommerce.DTO.AppUserDTO;
import com.akida.ecommerce.DTOMapper.AppUserDTOMapper;
import com.akida.ecommerce.Enumarators.AppUserRole;
import com.akida.ecommerce.email.EmailSender;
import com.akida.ecommerce.models.AppUser;
import com.akida.ecommerce.registration.token.ConfirmationToken;
import com.akida.ecommerce.registration.token.ConfirmationTokenRepository;
import com.akida.ecommerce.registration.token.ConfirmationTokenService;
import com.akida.ecommerce.repository.AppUserRepository;
import com.akida.ecommerce.security.JwtUtil;
import com.akida.ecommerce.services.AppUserService;
import com.akida.ecommerce.services.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final AppUserService appUserService;
    private final ConfirmationTokenService confirmationTokenService;
    private EmailValidator emailValidator;
    private final EmailSender emailSender;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AppUserRepository appUserRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final AppUserDTOMapper appUserDTOMapper;
    private final JwtUtil jwtUtil;
    private AuthenticationManager authenticationManager;

    @Override
    public String register(RegistrationRequest request) {
        boolean isValidEmail=emailValidator.test(request.email());
        if(!isValidEmail){
            //throw new IllegalStateException("email n'est pas validee");
            return ("email n'est pas validee");
        }


        String token = appUserService.signUpUser(
                new AppUser(
                        request.first_name(),
                        request.last_name(),
                        request.email(),
                        request.password(),
                        AppUserRole.valueOf(String.valueOf(request.app_user_role()))


                )
        );

        String link = "http://localhost:8081/api/v1/registration/confirm?token=" + token;
        emailSender.send(
                request.email(),"pfe@leoni.com",
                buildEmail(request.email(), request.password(), request.first_name(), link));

        return token;
    }




    @Override
    public List<AppUserDTO> findAllAppUsers() {
        return appUserRepository.findAll()
                .stream()
                .map(appUserDTOMapper)
                .collect(Collectors.toList());
    }


    @Override
    public void updateAppUser(RegistrationRequest request, Long id) {
        AppUser exist = appUserRepository.findById(id).orElse(null);
        assert exist != null;
        AppUser appUser;
        /*if(request.password().isEmpty()){
            appUser = new AppUser(
                    id,
                    request.first_name(),
                    request.last_name(),
                    request.email(),
                    exist.getPassword(),
                    AppUserRole.valueOf(String.valueOf(request.app_user_role())),
                    exist.isEnabled()


            );

        }else {
            appUser = new AppUser(
                    id,
                    request.first_name(),
                    request.last_name(),
                    request.email(),
                    request.password(),
                    AppUserRole.valueOf(String.valueOf(request.app_user_role())),
                    exist.isEnabled()

            );
        }
        appUserRepository.save(appUser);*/


    }
    @Override
    public Optional<AppUserDTO> findById(Long id){
        return appUserRepository.findById(id).map(appUserDTOMapper);
    }

    @Override
    public void deleteAppUser(Long id) {
        if (confirmationTokenRepository.findById(id).isPresent()) {
            confirmationTokenRepository.deleteById(id);
            appUserRepository.deleteById(id);
        }
    }

    @Override
    public List<AppUserDTO> findByRole(AppUserRole role) {
        List<AppUser> appUsers = appUserRepository.findByAppUserRole(role);
        return appUsers.stream().map(appUserDTOMapper).collect(Collectors.toList());
    }


    @Override
    public HashMap<String,String> login(@RequestBody LoginRequest loginRequest){
        HashMap<String, String> map = new HashMap<>();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        AppUser appUser = appUserRepository.findByEmail(loginRequest.email()).orElse(null);
        assert appUser != null;
        String role=appUser.getAppUserRole().toString();

        if (!encoder.matches(loginRequest.password(), appUser.getPassword())) {
            map.put("response", "password");
            return map;


        } else if ((appUser.isEnabled()) &&
                encoder.matches(loginRequest.password(), appUser.getPassword())) {
            map.put("response", role);
            map.put("FirstName", appUser.getFirstName());
            map.put("LastName", appUser.getLastName());
            map.put("currentUserId", appUser.getId().toString());
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
            map.put("token", jwtUtil.generateToken(authentication));


            return map;
        } else if (!appUser.isEnabled()) {
            map.put("response", "disabled");
            return map;
        }
        return null;
    }


    @Override
    public HashMap<String,String> ChangePWD(Long id, PwdRequest newPWD){
        HashMap<String, String> map = new HashMap<>();
        AppUser appUser=appUserRepository.findById(id).orElse(null);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assert appUser != null;
        if(!encoder.matches(newPWD.current(), appUser.getPassword())){
            map.put("response", "wrong password");
        } else if (!newPWD.newPWD().equals(newPWD.renew())) {
            map.put("response", "password mismatch");
        }else {
            appUser.setPassword(bCryptPasswordEncoder.encode(newPWD.newPWD()));
            map.put("response", "success");
            appUserRepository.save(appUser);
        }



        return map;

    }



    @Transactional
    @Override
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() ->
                        new IllegalStateException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            return "email already confirmed";
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            return "token expired";
        }

        confirmationTokenService.setConfirmedAt(token);
        appUserService.enableAppUser(
                confirmationToken.getAppUser().getEmail());
        return """    
                <div style="
                        padding: 20px;
                        background-color: #f0f8ff;
                        border: 2px solid #4682b4;
                        border-radius: 10px;
                        color: #2e4a6b;
                        font-family: Arial, sans-serif;
                        text-align: center;
                        max-width: 400px;
                        margin: 20px auto;
                        box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                        ">
                                <h2 style="color: #4682b4; margin-top: 0;">✅ Confirmation</h2>
                                <p>Your action has been successfully completed!</p>
                                </div>
              """;
    }

    private String buildEmail(String email,String password,String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Bonjour " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <br><br>Ci-dessous, vous trouvez le User et Mot de passe<br><br>username : " +email +" \n .<br>Mot de passe : "+password+".<br><br><br> . Veuillez cliquer sur le lien ci-dessous pour activer votre compte : </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Le lien expirera dans 15 minutes." +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }
}
