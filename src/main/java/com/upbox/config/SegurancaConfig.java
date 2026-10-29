package com.upbox.config;

import com.upbox.repository.UsuarioRepository;
import com.upbox.service.AuditoriaService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/** Segurança convencional de formulário: o servidor controla sessão, papéis e CSRF. */
@Configuration
@RequiredArgsConstructor
public class SegurancaConfig {
    private final UsuarioRepository usuarios;
    private final AuditoriaService auditoria;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return login -> {
            var encontrados =
                    usuarios.findByUsuarioIgnoreCaseOrEmailIgnoreCase(login.trim(), login.trim());
            if (encontrados.size() != 1)
                throw new UsernameNotFoundException("Usuário ou senha inválidos.");
            var u = encontrados.get(0);
            // O principal guarda o ID estável, não o nome enviado no formulário.
            return new UsuarioAutenticado(
                    u.getId(), u.getSenhaHash(), u.getPerfil().name(), u.isAtivo());
        };
    }

    @Bean
    SecurityFilterChain seguranca(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                        a ->
                                a.requestMatchers(
                                                "/",
                                                "/login",
                                                "/css/**",
                                                "/images/**",
                                                "/js/interface.js",
                                                "/error")
                                        .permitAll()
                                        .requestMatchers(
                                                "/usuarios/**",
                                                "/cadastro",
                                                "/categorias/**",
                                                "/produtos/**",
                                                "/registros",
                                                "/relatorios/imprimir")
                                        .hasRole("ADMINISTRADOR")
                                        .anyRequest()
                                        .authenticated())
                .formLogin(
                        f ->
                                f.loginPage("/login")
                                        .successHandler(
                                                (req, res, auth) -> {
                                                    auditoria.registrar(
                                                            "Login", "Acesso ao sistema");
                                                    res.sendRedirect(
                                                            req.getContextPath() + "/home");
                                                })
                                        .failureUrl("/login?erro")
                                        .permitAll())
                .logout(
                        l ->
                                l.addLogoutHandler(
                                                (req, res, auth) ->
                                                        auditoria.registrar(
                                                                "Logout", "Sessão encerrada"))
                                        .logoutSuccessUrl("/login?logout"))
                .exceptionHandling(e -> e.accessDeniedPage("/acesso-negado"))
                .addFilterBefore(
                        new SessaoAtualFilter(usuarios), AnonymousAuthenticationFilter.class);
        // CSRF permanece habilitado; formulários Thymeleaf incluem o token automaticamente.
        return http.build();
    }
}
