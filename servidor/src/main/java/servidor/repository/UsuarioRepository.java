package servidor.repository;

import servidor.model.*;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UsuarioRepository extends JpaRepository<Usuario, String>{

}
