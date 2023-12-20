package servidor.model;


import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;


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
    private Set<Usuario> amistades;

    @ManyToMany(mappedBy = "amistades")
    @JsonSerialize(using = SetUsuarioSerializer.class)
    private Set<Usuario> amistades2;

    public Usuario(){
        amistades = new HashSet<>();
        solicitudesAmistad = new HashSet<>();
    }

    public Usuario(String username, String password){
        this.username = username;
        this.password = password;
        amistades = new HashSet<>();
        solicitudesAmistad = new HashSet<>();
    }

    // Getters
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

    // Setters
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

    // Métodos publicos

    public boolean addSolicitud(Usuario solicitante){
        return solicitudesAmistad.add(solicitante);
    }

    public boolean replySolicitud(Usuario solicitante, boolean reply){
        if (!solicitudesAmistad.remove(solicitante))
            return false;
        if (!reply)
            return true;
        return addAmigo(solicitante);
    }

    public boolean removeAmigo(Usuario amigo){
        return amistades.remove(amigo) || amistades2.remove(amigo);
    }

    // Metodos privados
    private boolean addAmigo(Usuario amigo){
        amigo.addAmigo2(this);
        return amistades.add(amigo);
    }

    private boolean addAmigo2(Usuario amigo){
        return amistades2.add(amigo);
    }

    
    //
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