package com.upbox.service;

import com.upbox.model.RegistroOperacao;
import com.upbox.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Participa da transação da operação; nunca recebe senhas como detalhe. */
@Service
@RequiredArgsConstructor
public class AuditoriaService {
    private final RegistroOperacaoRepository registros;
    private final UsuarioRepository usuarios;

    @Transactional
    public void registrar(String acao, String detalhe) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String responsavel = "Sistema";
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            try {
                responsavel =
                        usuarios.findById(Long.valueOf(auth.getName()))
                                .map(u -> u.getUsuario() + " (#" + u.getId() + ")")
                                .orElse("Sessão encerrada");
            } catch (NumberFormatException ignored) {
                responsavel = "Sistema";
            }
        }
        registros.save(new RegistroOperacao(responsavel, acao, detalhe));
    }
}
