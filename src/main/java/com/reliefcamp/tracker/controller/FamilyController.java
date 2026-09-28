package com.reliefcamp.tracker.controller;

import com.reliefcamp.tracker.model.Family;
import com.reliefcamp.tracker.service.FamilyService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/families")
public class FamilyController {
    private final FamilyService service;
    public FamilyController(FamilyService service) { this.service = service; }
    @GetMapping public List<Family> list(@RequestParam(required = false) Long campId) { return service.list(campId); }
    @GetMapping("/{id}") public Family get(@PathVariable Long id) { return service.getOne(id); }
    @PostMapping public ResponseEntity<Family> create(@Valid @RequestBody Family family) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(family)); }
    @PutMapping("/{id}") public Family update(@PathVariable Long id, @Valid @RequestBody Family family) { return service.update(id, family); }
    @PutMapping("/{id}/check-in") public Family checkIn(@PathVariable Long id, @RequestBody Family family) { return service.checkIn(id, family); }
    @PutMapping("/{id}/check-out") public Family checkOut(@PathVariable Long id) { return service.checkOut(id); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
