package com.cheikh.commun.core;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

public abstract class GenericController<T extends GenericEntity<T>, D, R> {
    /*
     * D : ResponseDto
     * R : ResquestDto
     * T : entity
     *  */
    private final GenericService<T, D, R> service;
    protected Class<T> TClass;
    protected Class<D> DClass;

    public GenericController(GenericRepository<T> repository, Class<T> TClass, Class<D> DClass) {
        this.service = new GenericService<T, D, R>(repository, TClass, DClass) {
        };
    }


    @GetMapping("/{id}")
    public ResponseEntity<D> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping("")
    public ResponseEntity<D> update(@RequestBody T updated) {
        return ResponseEntity.ok(service.update(updated));
    }

    @PostMapping("")
    public ResponseEntity<D> create(@RequestBody T created) {
        return ResponseEntity.ok(service.create(created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<D> delete(@PathVariable Long id) {
        D deleted = service.delete(id);
        return ResponseEntity.ok(deleted);
    }
}
