package com.upbox.model;

import jakarta.persistence.*;

/**
 * Usuário interno do sistema.
 *
 * A senha será armazenada somente como hash (por exemplo, BCrypt).
 * Esta classe não realiza login; a autenticação será implementada depois.
 */
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

    protected Usuario() {
    }

    /**
     * Recebe uma senha que JÁ foi transformada em hash.
     * Nunca envie a senha digitada em texto puro para este construtor.
     */
    public Usuario(String nome, String usuario, String email,
                   String senhaHash, PerfilAcesso perfil) {
        if (nome == null || nome.isBlank()
                || usuario == null || usuario.isBlank()
                || email == null || email.isBlank()
                || senhaHash == null || senhaHash.isBlank()
                || perfil == null) {
            throw new IllegalArgumentException("Preencha os dados obrigatórios do usuário.");
        }
        this.nome = nome.trim();
        this.usuario = usuario.trim();
        this.email = email.trim();
        this.senhaHash = senhaHash;
        this.perfil = perfil;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public PerfilAcesso getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void desativar() {
        this.ativo = false;
    }
}
