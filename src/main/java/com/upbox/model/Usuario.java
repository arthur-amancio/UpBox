package com.upbox.model;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Usuário interno do sistema.
 *
 * <p>A senha será armazenada somente como hash (por exemplo, BCrypt). A autenticação é
 * responsabilidade do Spring Security, não desta entidade.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 80)
    private String usuario;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilAcesso perfil;

    @Column(nullable = false)
    private boolean ativo = true;

    /**
     * Recebe uma senha que JÁ foi transformada em hash. Nunca envie a senha digitada em texto puro
     * para este construtor.
     */
    public Usuario(
            String nome, String usuario, String email, String senhaHash, PerfilAcesso perfil) {
        if (nome == null
                || nome.isBlank()
                || usuario == null
                || usuario.isBlank()
                || email == null
                || email.isBlank()
                || senhaHash == null
                || senhaHash.isBlank()
                || perfil == null) {
            throw new IllegalArgumentException("Preencha os dados obrigatórios do usuário.");
        }
        this.nome = nome.trim();
        this.usuario = usuario.trim();
        this.email = email.trim();
        this.senhaHash = senhaHash;
        this.perfil = perfil;
    }

    /** Mudanças são validadas pelo serviço antes de chegar à entidade. */
    public void atualizar(String nome, String usuario, String email, PerfilAcesso perfil) {
        this.nome = nome.trim();
        this.usuario = usuario.trim().toLowerCase(java.util.Locale.ROOT);
        this.email = email.trim().toLowerCase(java.util.Locale.ROOT);
        this.perfil = perfil;
    }

    public void alterarSenhaHash(String hash) {
        this.senhaHash = hash;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }
}
