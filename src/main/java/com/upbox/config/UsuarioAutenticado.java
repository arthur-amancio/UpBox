package com.upbox.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/** Principal da sessão: guarda ID estável e permite revogar sessões após troca da senha. */
public final class UsuarioAutenticado implements UserDetails {
    private final Long id;
    private final String hash;
    private final String perfil;
    private final boolean ativo;

    public UsuarioAutenticado(Long id, String hash, String perfil, boolean ativo) {
        this.id = id;
        this.hash = hash;
        this.perfil = perfil;
        this.ativo = ativo;
    }

    @Override
    public String getUsername() {
        return id.toString();
    }

    @Override
    public String getPassword() {
        return hash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil));
    }

    @Override
    public boolean isEnabled() {
        return ativo;
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
    public String toString() {
        return "Usuário autenticado #" + id;
    }
}
