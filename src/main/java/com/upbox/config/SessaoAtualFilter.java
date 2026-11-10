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
        String caminho = req.getRequestURI().substring(req.getContextPath().length());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                && req.getRequestedSessionId() != null
                && !req.isRequestedSessionIdValid()
                && !caminho.equals("/login")
                && !caminho.startsWith("/css/")
                && !caminho.startsWith("/images/")
                && !caminho.startsWith("/js/")
                && !caminho.startsWith("/fonts/")) {
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
            // Uma URL digitada manualmente também precisa respeitar a troca inicial.
            boolean permitido =
                    (caminho.equals("/configuracoes") && req.getMethod().equals("GET"))
                            || caminho.equals("/configuracoes/senha")
                            || caminho.equals("/logout")
                            || caminho.startsWith("/css/")
                            || caminho.startsWith("/images/")
                            || caminho.startsWith("/fonts/")
                            || caminho.startsWith("/js/");
            if (u.isTrocaSenhaObrigatoria() && !permitido) {
                res.sendRedirect(req.getContextPath() + "/configuracoes");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
