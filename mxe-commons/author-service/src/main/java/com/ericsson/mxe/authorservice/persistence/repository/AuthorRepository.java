package com.ericsson.mxe.authorservice.persistence.repository;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.ericsson.mxe.authorservice.dto.Author;
import com.ericsson.mxe.authorservice.dto.VerifyResponse;
import com.ericsson.mxe.authorservice.persistence.domain.AuthorEntity;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import com.ericsson.mxe.backendservicescommon.exception.MxeResourceNotFoundException;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface AuthorRepository extends CrudRepository<AuthorEntity, String> {
    default AuthorEntity save(Author author) {
        String name = author.getName().trim();
        if (existsById(name))
            throw new MxeConflictException("Author with this name already exists");
        if (findFirstAuthorEntityByPublicKey(author.getPublicKey()).result)
            throw new MxeConflictException("Author with this private key aready exists");
        return save(new AuthorEntity(name, author.getPublicKey()));
    }

    default Iterable<Author> findAllAuthor() {
        return StreamSupport.stream(findAll().spliterator(), false)
                .map(authorEntity -> new Author(authorEntity.getName(), authorEntity.getPublicKey()))
                .collect(Collectors.toList());
    }

    default void deleteOrThrow(final String name) {
        if (existsById(name)) {
            deleteById(name);
        } else {
            throw new MxeResourceNotFoundException("Author not found");
        }
    }

    default VerifyResponse findFirstAuthorEntityByPublicKey(final String publicKey) {
        String normalizedpublicKey = normalizePublicKey(publicKey);
        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(author -> normalizedpublicKey.equals(normalizePublicKey(author.getPublicKey()))).findAny()
                .map(entity -> new VerifyResponse(true, entity.getName())).orElse(new VerifyResponse(false, null));
    }

    private String normalizePublicKey(String publicKey) {
        // Remove potential public key header and footer according to RFC 7468 section 13
        String trimmedKey =
                StringUtils.substringBetween(publicKey, "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");

        return StringUtils.deleteWhitespace(trimmedKey != null ? trimmedKey : publicKey);
    }
}
