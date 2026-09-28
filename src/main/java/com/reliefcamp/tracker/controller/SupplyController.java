package com.reliefcamp.tracker.controller;

import com.reliefcamp.tracker.model.Supply;
import com.reliefcamp.tracker.service.SupplyService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/supplies")
public class SupplyController {
    private final SupplyService service;
    public SupplyController(SupplyService service) { this.service = service; }
    @GetMapping public List<Supply> list(@RequestParam(required = false) Long campId) { return service.list(campId); }
    @GetMapping("/{id}") public Supply get(@PathVariable Long id) { return service.getOne(id); }
    @PostMapping public ResponseEntity<Supply> create(@Valid @RequestBody Supply supply) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(supply)); }
    @PutMapping("/{id}") public Supply update(@PathVariable Long id, @Valid @RequestBody Supply supply) { return service.update(id, supply); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
