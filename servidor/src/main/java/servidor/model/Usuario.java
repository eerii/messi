package servidor.model;

import shared.ICliente;
import jakarta.persistence.*;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.HashSet;


@Entity
public class Usuario{

    @Id
    private String username;

    private String password;

    @Transient
    @JsonIgnore
    private ICliente conexion;

    // Solicitudes que el Usuario tiene que aceptar o rechazar
    @ManyToMany
    @JoinTable(
            name = "solicitudes_amistad",
            joinColumns = @JoinColumn(name = "requested"),
            inverseJoinColumns = @JoinColumn(name = "requester"))
    @JsonSerialize(using = SetUsuarioSerializer.class)
    private Set<Usuario> solicitudesAmistad;

    @ManyToMany
    @JoinTable(
            name = "amistades",
            joinColumns = @JoinColumn(name = "amigo1"),
            inverseJoinColumns = @JoinColumn(name = "amigo2"))
    @JsonSerialize(using = SetUsuarioSerializer.class)
    private Set<Usuario> amistades;

    public Usuario(){
        amistades = new HashSet<>();
        solicitudesAmistad = new HashSet<>();
    }


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<Usuario> getSolicitudesAmistad() {
        return solicitudesAmistad;
    }

    public Set<Usuario> getAmistades() {
        return amistades;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSolicitudesAmistad(Set<Usuario> solicitudesAmistad) {
        this.solicitudesAmistad = solicitudesAmistad;
    }

    public void setAmistades(Set<Usuario> amistades) {
        this.amistades = amistades;
    }


    @Override
    public String toString() {
        return "Usuario [username=" + username + ", password=" + password + "]";
    }

}