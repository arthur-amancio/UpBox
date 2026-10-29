package com.upbox.repository;

import com.upbox.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso aos usuários. O login real ainda não foi implementado. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
