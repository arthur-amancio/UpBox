package com.upbox.service;

import com.upbox.entity.*;
import com.upbox.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Cria a conta padrão automaticamente; a senha de uso é escolhida na ativação local. */
@Service
@RequiredArgsConstructor
public class PrimeiroAcessoService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoria;

    @Transactional(readOnly = true)
    public boolean disponivel() {
        return usuarios.findByUsuarioIgnoreCase("administrador")
                .filter(u -> u.isAtivo() && u.isAtivacaoPendente())
                .isPresent();
    }

    /** A restrição UNIQUE do login decide qual instância pode criar a primeira conta. */
    @Transactional
    public void criarContaPadrao() {
        if (usuarios.existsByPerfil(PerfilAcesso.ADMINISTRADOR)) return;
        if (usuarios.findByUsuarioIgnoreCase("administrador").isPresent())
            throw new IllegalArgumentException(
                    "O login administrador já existe. Revise a conta sem sobrescrevê-la.");
        // Não existe senha pública: este valor aleatório não é exibido nem permite login pendente.
        String segredo = UUID.randomUUID().toString() + UUID.randomUUID();
        Usuario u =
                new Usuario(
                        "Administrador",
                        "administrador",
                        "administrador@upbox.local",
                        encoder.encode(segredo),
                        PerfilAcesso.ADMINISTRADOR);
        u.exigirAtivacao();
        usuarios.saveAndFlush(u);
    }

    /** O bloqueio da linha impede duas ativações de alterarem a mesma senha. */
    @Transactional
    public void ativar(String senha) {
        var u =
                usuarios.administradorComBloqueio()
                        .filter(x -> x.isAtivo() && x.isAtivacaoPendente())
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "A ativação inicial já foi concluída."));
        UsuarioService.validarSenha(senha);
        u.alterarSenhaHash(encoder.encode(senha));
        u.concluirAtivacao();
        auditoria.registrar("Administrador ativado", "Senha definida no computador da instalação");
    }
}
