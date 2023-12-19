package servidor.model;

import shared.ICliente;
import jakarta.persistence.*;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.HashSet;


@Entity
public class Usuario{

    @Id
    private String username;

    private String password;

    // Solicitudes que el Usuario tiene que aceptar o rechazar
    @ManyToMany
    @JoinTable(
            name = "solicitudes_amistad",
            joinColumns = @JoinColumn(name = "requested"),
            inverseJoinColumns = @JoinColumn(name = "requester"))
    @JsonSerialize(using = SetUsuarioSerializer.class)
    @Fetch(FetchMode.JOIN)
    private Set<Usuario> solicitudesAmistad;

    @ManyToMany
    @JoinTable(
            name = "amistades",
            joinColumns = @JoinColumn(name = "amigo1"),
            inverseJoinColumns = @JoinColumn(name = "amigo2"))
    @JsonSerialize(using = SetUsuarioSerializer.class)
    @Fetch(FetchMode.JOIN)
    private Set<Usuario> amistades;

    @ManyToMany(mappedBy = "amistades")
    @JsonSerialize(using = SetUsuarioSerializer.class)
    @Fetch(FetchMode.JOIN)
    private Set<Usuario> amistades2;

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

    public Set<Usuario> getAmistades2() {
        return amistades2;
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
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Usuario other = (Usuario) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Usuario [username=" + username + ", password=" + password + "]";
    }

}