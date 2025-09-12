package Auction.Auction.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "user")  // Assuming table name is 'user'
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;  // Default to USER
    private boolean isVerified = false;

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;  // Use email as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Or implement expiration logic if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Or implement lock logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Or implement credential expiration
    }

    @Override
    public boolean isEnabled() {
        return isVerified;  // Only enabled if verified
    }
}