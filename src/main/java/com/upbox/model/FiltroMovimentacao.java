package com.upbox.model;

import lombok.Data;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.*;

/** Filtros compartilhados entre histórico e relatórios; datas finais incluem o dia inteiro. */
@Data
public class FiltroMovimentacao {
    private Long produto;
    private Long categoria;
    private Long usuario;
    private TipoMovimentacao tipo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate inicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fim;

    public void validar() {
        if ((inicio != null && (inicio.getYear() < 1 || inicio.getYear() > 9999))
                || (fim != null && (fim.getYear() < 1 || fim.getYear() > 9999)))
            throw new IllegalArgumentException("Informe datas entre os anos 0001 e 9999.");
        if (inicio != null && fim != null && inicio.isAfter(fim))
            throw new IllegalArgumentException("A data inicial não pode ser posterior à final.");
    }

    public LocalDateTime inicioEfetivo() {
        return inicio == null ? LocalDateTime.of(1, 1, 1, 0, 0) : inicio.atStartOfDay();
    }

    public LocalDateTime fimExclusivo() {
        return fim == null ? LocalDateTime.of(10000, 1, 1, 0, 0) : fim.plusDays(1).atStartOfDay();
    }
}
