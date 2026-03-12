package com.kinforgework.cplaneta.controller;

import com.kinforgework.cplaneta.dto.AreaRequest;
import com.kinforgework.cplaneta.dto.AreaResponse;
import com.kinforgework.cplaneta.service.AreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @GetMapping
    public ResponseEntity<List<AreaResponse>> findAll() {
        return ResponseEntity.ok(areaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(areaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<List<AreaResponse>> create(@Valid @RequestBody List<AreaRequest> request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(areaService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AreaResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AreaRequest request
    ) {
        return ResponseEntity.ok(areaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        areaService.delete(id);
        return ResponseEntity.noContent()
                .build();
    }
}
