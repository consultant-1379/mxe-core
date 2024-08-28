package com.ericsson.mxe.authorservice.persistence.domain;

import org.hibernate.annotations.Immutable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Immutable
@Table(name = "authors", indexes = {@Index(columnList = "publicKey", name = "authors_public_key_index")})
public class AuthorEntity {
    @Id
    @Column
    private String name;

    @Column
    @Lob
    private String publicKey;

    public AuthorEntity() {}

    public AuthorEntity(String name, String publicKey) {
        this.publicKey = publicKey;
        this.name = name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getName() {
        return name;
    }
}
