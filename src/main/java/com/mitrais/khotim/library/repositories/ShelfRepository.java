package com.mitrais.khotim.library.repositories;

import com.mitrais.khotim.library.domains.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelfRepository extends JpaRepository<Shelf, Long> {
}
