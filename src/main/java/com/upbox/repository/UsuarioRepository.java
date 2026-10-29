package com.upbox.repository;

import com.upbox.model.*;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;

import java.util.*;

/** Identificação sem diferenciar maiúsculas; bloqueio protege o último administrador. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findByUsuarioIgnoreCaseOrEmailIgnoreCase(String usuario, String email);

    boolean existsByPerfilAndAtivoTrue(PerfilAcesso perfil);

    boolean existsByPerfil(PerfilAcesso perfil);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u order by u.id")
    List<Usuario> listarComBloqueio();
}
