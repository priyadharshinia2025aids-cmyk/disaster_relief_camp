package com.reliefcamp.tracker.controller;

import com.reliefcamp.tracker.model.Distribution;
import com.reliefcamp.tracker.service.DistributionService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/distributions")
public class DistributionController {
    private final DistributionService service;
    public DistributionController(DistributionService service) { this.service = service; }
    @GetMapping public List<Distribution> list() { return service.list(); }
    @PostMapping public ResponseEntity<Distribution> create(@Valid @RequestBody Distribution distribution) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(distribution)); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
