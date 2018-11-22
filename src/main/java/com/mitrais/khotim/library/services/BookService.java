package com.mitrais.khotim.library.services;

import com.mitrais.khotim.library.domains.Book;
import com.mitrais.khotim.library.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    @Autowired
    private BookRepository repository;

    public List<Book> findByTitleAndStatus(String title, String status) {
        if (title.isEmpty() && status.isEmpty()) {
            return repository.findAll();
        }

        if (status.isEmpty()) {
            return repository.findByTitleContainingIgnoreCase(title);
        }

        if (title.isEmpty()) {
            return repository.findByStatusIgnoreCase(status);
        }

        return repository.findByTitleContainingIgnoreCaseAndStatusIgnoreCase(title, status);
    }

    public Optional<Book> findById(Long id) {
        return repository.findById(id);
    }

    public Book save(Book newBook) {
        return repository.save(newBook);
    }
}
