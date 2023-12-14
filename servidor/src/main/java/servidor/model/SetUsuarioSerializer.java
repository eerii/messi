package servidor.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SetUsuarioSerializer extends StdSerializer<Set<Usuario>> {

    public SetUsuarioSerializer() {
        this(null);
    }

    public SetUsuarioSerializer(Class<Set<Usuario>> t) {
        super(t);
    }

    @Override
    public void serialize(Set<Usuario> usuarios, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Set<String> usernames = new HashSet<>();
        for (Usuario usuario : usuarios) {
            usernames.add(usuario.getUsername());
        }
        jsonGenerator.writeObject(usernames);
    }
}