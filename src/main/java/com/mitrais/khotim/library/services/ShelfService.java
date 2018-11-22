package com.mitrais.khotim.library.services;

import com.mitrais.khotim.library.domains.Book;
import com.mitrais.khotim.library.domains.Shelf;
import com.mitrais.khotim.library.repositories.ShelfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShelfService {
    @Autowired
    private ShelfRepository shelfRepository;

    /**
     * Adds book into shelf.
     *
     * @param shelf
     * @param book
     * @return
     */
    public Shelf addBook(Shelf shelf, Book book) {
        book.setStatus(Book.SHELVED);
        shelf.setCurrentCapacity(shelf.getCurrentCapacity() + 1);
        shelf.addBook(book);

        return shelfRepository.save(shelf);
    }

    /**
     * Removes book from shelf.
     *
     * @param shelf
     * @param book
     * @return
     */
    public Shelf removeBook(Shelf shelf, Book book) {
        book.setStatus(Book.NOT_SHELVED);
        shelf.setCurrentCapacity(shelf.getCurrentCapacity() - 1);
        shelf.removeBook(book);

        return shelfRepository.save(shelf);
    }

    public Optional<Shelf> findById(Long id) {
        return shelfRepository.findById(id);
    }

    public List<Shelf> findAll() {
        return shelfRepository.findAll();
    }

    public Shelf save(Shelf shelf) {
        return shelfRepository.save(shelf);
    }
}
