package servidor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import servidor.model.Usuario;
import servidor.repository.UsuarioRepository;
import shared.ICliente;

@Service
public class UsuarioService {
    
    @Autowired
    UsuarioRepository usuarioRepository;

    @Transactional
    public Usuario getByUsername(String username){
        Usuario user = usuarioRepository.findById(username).get();
        return user;
    }

    @Transactional
    public boolean conectarUsuario(String username, ICliente conexion){
        return true;
    }

    @Transactional
    public boolean desconectarUsuario(String username){
        return true;
    }

}
