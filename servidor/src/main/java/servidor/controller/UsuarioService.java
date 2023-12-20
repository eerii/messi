package servidor.controller;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import servidor.model.Usuario;
import servidor.repository.UsuarioRepository;

@Service
public class UsuarioService {
    
    @Autowired
    UsuarioRepository usuarioRepository;

    // Sesion
    @Transactional
    public boolean signup(String username, String password){
        if(usuarioRepository.existsById(username))
            return false;
        Usuario usuario = new Usuario(username, password);
        usuarioRepository.saveAndFlush(usuario);
        return true;
    }

    @Transactional
    public boolean login(String username, String password){
        Usuario usuario = loadUser(username);
        if(usuario == null || !usuario.getPassword().equals(password))
            return false;
        return true;
    }

    @Transactional
    public boolean unsubscribe(String username){
        if (!usuarioRepository.existsById(username))
            return false;
        usuarioRepository.deleteAllByIdInBatch(Set.of(username));
        usuarioRepository.flush();
        return true;
    }

    // Actualizacion
    @Transactional
    public boolean addSolicitud(String username, String solicitanteUsername){
        Usuario usuario     = loadUser(username);
        Usuario solicitante = loadUser(solicitanteUsername);
        if (usuario == null || solicitante == null)
            return false;
        boolean flag = usuario.addSolicitud(solicitante);
        usuarioRepository.saveAllAndFlush(Set.of(usuario, solicitante));
        return flag;
    }

    @Transactional
    public boolean replySolicitud(String username, String solicitanteUsername, boolean reply){
        Usuario usuario     = loadUser(username);
        Usuario solicitante = loadUser(solicitanteUsername);
        if (usuario == null || solicitante == null)
            return false;
        boolean flag = usuario.replySolicitud(solicitante, reply);
        usuarioRepository.saveAllAndFlush(Set.of(usuario, solicitante));
        return flag;
    }

    @Transactional
    public boolean changePassword(String username, String oldPassword, String newPassword){
        Usuario usuario = loadUser(username);
        if (usuario == null || !usuario.getPassword().equals(oldPassword))
            return false;
        usuario.setPassword(newPassword);
        usuarioRepository.saveAndFlush(usuario);
        return true;
    }

    @Transactional
    public boolean removeAmigo(String username, String amigoUsername){
        Usuario usuario = loadUser(username);
        Usuario amigo   = loadUser(amigoUsername);
        if (usuario == null || amigo == null)
            return false;
        if (!usuario.removeAmigo(amigo) || !amigo.removeAmigo(usuario))
            return false;
        usuarioRepository.saveAllAndFlush(Set.of(usuario, amigo));
        return true ;
    }

    // Consulta
    @Transactional
    public Set<String> getAmigos(String username){
        Usuario usuario = loadUser(username);
        if (usuario == null)
            return new HashSet<>();

        Set<Usuario> amistades  = usuario.getAmistades();
        Set<Usuario> amistades2 = usuario.getAmistades2();
        Set<String> amigos      = new HashSet<>();

        amistades. forEach(a -> amigos.add(a.getUsername()));
        amistades2.forEach(a -> amigos.add(a.getUsername()));

        return amigos;
    }

    @Transactional
    public Set<String> getSolicitudes(String username){
        Usuario usuario = loadUser(username);
        if (usuario == null)
            return new HashSet<>();
        
        Set<Usuario> solicitudesAmistad = usuario.getSolicitudesAmistad();
        Set<String>  solicitudes  = new HashSet<>();

        solicitudesAmistad.forEach(s -> solicitudes.add(s.getUsername()));
        return solicitudes;
    }

    @Transactional
    public boolean existsUser(String username){
        return usuarioRepository.existsById(username);
    }

    @Transactional
    private Usuario loadUser (String username){
        Optional<Usuario> UsuarioData = usuarioRepository.findById(username);
        if(!UsuarioData.isPresent())
            return null;
        return UsuarioData.get();
    }

}
