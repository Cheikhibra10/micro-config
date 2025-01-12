package com.cheikh.commun.core;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GenericRepository<T extends GenericEntity<T>> extends JpaRepository<T, Long> {
}
