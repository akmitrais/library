package com.mitrais.khotim.library.repositories;

import com.mitrais.khotim.library.domains.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByStatusIgnoreCase(String status);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByTitleContainingIgnoreCaseAndStatusIgnoreCase(String title, String status);
}
