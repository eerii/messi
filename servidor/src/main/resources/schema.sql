-- Creación de la tabla UsuarioEntity
CREATE TABLE usuario (
    username VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

-- Creación de la tabla de solicitudes de amistad
CREATE TABLE solicitudes_amistad (
    requester VARCHAR(255),
    requested VARCHAR(255),
    PRIMARY KEY (requester, requested),
    FOREIGN KEY (requester) REFERENCES Usuario(username),
    FOREIGN KEY (requested) REFERENCES Usuario(username)
);

-- Creación de la tabla de amistades
CREATE TABLE amistades (
    amigo1 VARCHAR(255),
    amigo2 VARCHAR(255),
    PRIMARY KEY (amigo1, amigo2),
    FOREIGN KEY (amigo1) REFERENCES Usuario(username),
    FOREIGN KEY (amigo2) REFERENCES Usuario(username)
);

