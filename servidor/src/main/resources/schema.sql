-- Creación de la tabla UsuarioEntity
CREATE TABLE Usuario (
    username VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

-- Creación de la tabla de solicitudes de amistad
CREATE TABLE solicitudes_amistad (
    usuario_id VARCHAR(255) REFERENCES Usuario(username),
    amigo_id VARCHAR(255) REFERENCES Usuario(username)
);

-- Creación de la tabla de amistades
CREATE TABLE amistades (
    usuario_id VARCHAR(255) REFERENCES Usuario(username),
    amigo_id VARCHAR(255) REFERENCES Usuario(username)
);