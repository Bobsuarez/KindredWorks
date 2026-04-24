package com.kinforgework.cplaneta.controller;


import com.kinforgework.cplaneta.dto.ContactRequest;
import com.kinforgework.cplaneta.dto.ContactResponse;
import com.kinforgework.cplaneta.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<List<ContactResponse>> findAll() {
        return ResponseEntity.ok(contactService.findAll());
    }

    @GetMapping("/{masterProgramId}")
    public ResponseEntity<ContactResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.findById(id));
    }

    @GetMapping("/by-program/{programId}")
    public ResponseEntity<List<ContactResponse>> findByProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(contactService.findByProgram(programId));
    }

    @GetMapping("/pending/count")
    public ResponseEntity<Long> countPending() {
        return ResponseEntity.ok(contactService.countPending());
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactService.create(request));
    }

    @DeleteMapping("/{masterProgramId}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent()
                .build();
    }
}
