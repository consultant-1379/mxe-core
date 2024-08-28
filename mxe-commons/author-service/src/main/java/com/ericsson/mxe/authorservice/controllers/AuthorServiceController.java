package com.ericsson.mxe.authorservice.controllers;

import com.ericsson.mxe.authorservice.dto.Author;
import com.ericsson.mxe.authorservice.dto.VerifyResponse;
import com.ericsson.mxe.authorservice.persistence.repository.AuthorRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/v1/authors")
public class AuthorServiceController {
    private final AuthorRepository authorRepository;

    public AuthorServiceController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @GetMapping
    public Iterable<Author> list() {
        return authorRepository.findAllAuthor();
    }

    @PostMapping
    public void add(@RequestBody @Validated Author author) {
        authorRepository.save(author);
    }

    @DeleteMapping("/{name}")
    public void delete(@PathVariable String name) {
        authorRepository.deleteOrThrow(name);
    }

    @PostMapping("/verify")
    public VerifyResponse verify(@RequestBody String publicKey) {
        return authorRepository.findFirstAuthorEntityByPublicKey(publicKey);
    }
}
