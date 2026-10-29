package com.upbox.config;

import com.upbox.repository.UsuarioRepository;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Revoga sessões com conta inativa, senha trocada ou papel desatualizado. */
@RequiredArgsConstructor
public class SessaoAtualFilter extends OncePerRequestFilter {
    private final UsuarioRepository usuarios;

    @Override
    protected void doFilterInternal(
            HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                && req.getRequestedSessionId() != null
                && !req.isRequestedSessionIdValid()
                && !req.getServletPath().equals("/login")
                && !req.getServletPath().startsWith("/css/")
                && !req.getServletPath().startsWith("/images/")
                && !req.getServletPath().startsWith("/js/")) {
            res.sendRedirect(req.getContextPath() + "/login?expirada");
            return;
        }
        if (auth != null && auth.getPrincipal() instanceof UserDetails principal) {
            var u = usuarios.findById(Long.valueOf(auth.getName())).orElse(null);
            if (u == null
                    || !u.isAtivo()
                    || !principal.getPassword().equals(u.getSenhaHash())
                    || auth.getAuthorities().stream()
                            .noneMatch(
                                    a -> a.getAuthority().equals("ROLE_" + u.getPerfil().name()))) {
                if (req.getSession(false) != null) req.getSession(false).invalidate();
                SecurityContextHolder.clearContext();
                res.sendRedirect(req.getContextPath() + "/login?expirada");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
