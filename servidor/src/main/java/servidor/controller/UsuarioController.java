package servidor.controller;

import servidor.model.Usuario;
import servidor.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class UsuarioController {

	@Autowired
	UsuarioRepository usuarioRepository;

	@GetMapping("/users")
	public ResponseEntity<List<Usuario>> getAllUsuarios() {
		System.out.println("adios");
		try {
			List<Usuario> Usuarios = new ArrayList<Usuario>();

			usuarioRepository.findAll().forEach(Usuarios::add);

			System.out.println(Usuarios + "hola");

			System.out.println("HOla");

			if (Usuarios.isEmpty()) {
                System.out.println("No se han encontrado usuarios");
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(Usuarios, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/users/{id}")
	public ResponseEntity<Usuario> getUsuarioById(@PathVariable("id") String id) {
		Optional<Usuario> UsuarioData = usuarioRepository.findById(id);

		if (UsuarioData.isPresent()) {
			return new ResponseEntity<>(UsuarioData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

}
