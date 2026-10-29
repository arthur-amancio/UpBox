package com.upbox.model;

import jakarta.persistence.*;

import lombok.*;

import java.time.*;

/** Auditoria simples: guarda identificação textual mesmo se o usuário mudar de nome. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "registros_operacoes")
public class RegistroOperacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 180)
    private String responsavel;

    @Column(nullable = false, length = 80)
    private String acao;

    @Column(nullable = false, length = 500)
    private String detalhe;

    public RegistroOperacao(String responsavel, String acao, String detalhe) {
        this.dataHora = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
        this.responsavel = responsavel;
        this.acao = acao;
        this.detalhe = detalhe;
    }
}
