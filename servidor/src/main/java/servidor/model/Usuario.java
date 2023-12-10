package servidor.model;
 
import jakarta.persistence.*;
import java.util.List;

@Entity
public class Usuario{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String username;

    private String password; // Nueva adición para la contraseña

    @ElementCollection
    @CollectionTable(name = "solicitudes_amistad", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "amigo_id")
    private List<String> solicitudesAmistad;

    @ElementCollection
    @CollectionTable(name = "amistades", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "amigo_id")
    private List<String> amistades;



    @Override
    public String toString() {
        return "Usuario [username=" + username + ", password=" + password + "]";
    }

    
    

    
}