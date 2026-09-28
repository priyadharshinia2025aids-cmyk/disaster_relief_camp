package com.reliefcamp.tracker.controller;

import com.reliefcamp.tracker.model.Camp;
import com.reliefcamp.tracker.service.CampService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/camps")
public class CampController {
    private final CampService service;
    public CampController(CampService service) { this.service = service; }

    @GetMapping
    public Page<Camp> list(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(defaultValue = "name") String sort,
                           @RequestParam(defaultValue = "asc") String direction) {
        return service.list(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort, direction);
    }
    @GetMapping("/{id}") public Camp get(@PathVariable Long id) { return service.getOne(id); }
    @PostMapping public ResponseEntity<Camp> create(@Valid @RequestBody Camp camp) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(camp)); }
    @PutMapping("/{id}") public Camp update(@PathVariable Long id, @Valid @RequestBody Camp camp) { return service.update(id, camp); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
