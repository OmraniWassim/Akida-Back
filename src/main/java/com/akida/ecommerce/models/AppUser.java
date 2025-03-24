package com.akida.ecommerce.models;


import com.akida.ecommerce.Enumarators.AppUserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


@Setter
@Getter
@NoArgsConstructor
@Entity
@AllArgsConstructor
public class AppUser implements UserDetails {
    @Id
    @SequenceGenerator(name="appuser_sequence",sequenceName ="appuser_sequence",allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "appuser_sequence")
    private Long id;
    private String FirstName;
    private String LastName;
    @Column(unique = true)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private AppUserRole appUserRole;
    private Boolean enabled=false;
    private Long telNumber;
    private String address;
    private Long SIN;
//    @OneToMany
//    private List<Commande> commandes;




    public AppUser(String FirstName,
                   String LastName,
                   String email,
                   String password,
                   AppUserRole appUserRole
    ) {
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.email = email;
        this.password = password;
        this.appUserRole = appUserRole;

    }

    //for creating and testing but can be removed
    public AppUser(String FirstName,
                   String LastName,
                   String email,
                   String password,
                   boolean enabled,
                   AppUserRole appUserRole
    ) {
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.email = email;
        this.password = password;
        this.enabled  = enabled;
        this.appUserRole = appUserRole;

    }
    public AppUser(Long id,
                   String FirstName,
                   String LastName,
                   String email,
                   String password,
                   AppUserRole appUserRole
    ) {
        this.id=id;
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.email = email;
        this.password = password;
        this.appUserRole = appUserRole;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(appUserRole.name());
        return Collections.singletonList(authority);
    }





    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}