package com.mitrais.khotim.library.apis;

import com.mitrais.khotim.library.domains.Book;
import com.mitrais.khotim.library.domains.Shelf;
import com.mitrais.khotim.library.services.BookService;
import com.mitrais.khotim.library.services.ShelfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/libraries")
public class LibraryController {
    private final ShelfService shelfService;
    private final BookService bookService;

    @Autowired
    public LibraryController(ShelfService shelfService, BookService bookService) {
        this.shelfService = shelfService;
        this.bookService = bookService;
    }

    @GetMapping
    public List<Shelf> getAll() {
        return shelfService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shelf> getOne(@PathVariable Long id) {
        return shelfService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/addBook")
    public ResponseEntity<?> addBook(@PathVariable long id, @RequestBody Book pBook) {
        return doOperation(id, pBook, "add");
    }

    @DeleteMapping("/{id}/removeBook")
    public ResponseEntity<?> removeBook(@PathVariable long id, @RequestBody Book pBook) {
        return doOperation(id, pBook, "remove");
    }

    private ResponseEntity<?> doOperation(long id, Book pBook, String operation) {
        Map<String, String> messages = new HashMap<>();
        HttpStatus httpStatus = HttpStatus.OK;

        Shelf shelf = shelfService.findById(id).orElse(null);

        if (shelf == null) {
            messages.put("shelf", "There's no shelf found with id " + id);
            return new ResponseEntity<>(messages, httpStatus);
        }

        if (operation.equals("add") && shelf.getCurrentCapacity() == shelf.getMaxCapacity()) {
            messages.put("shelf", "Shelf " + shelf.getName() + " already reached maximum capacity");
            return new ResponseEntity<>(messages, httpStatus);
        }

        Book book = bookService.findById(pBook.getId()).orElse(null);

        if (book == null) {
            messages.put("book", "There's no book found with id " + pBook.getId());
            return new ResponseEntity<>(messages, httpStatus);
        }

        if (operation.equals("remove")) {
            if (!shelf.getBooks().contains(book)) {
                messages.put("shelf", "There's no book " + book.getTitle() + " in shelf " + shelf.getName());
                return new ResponseEntity<>(messages, httpStatus);
            }

            return new ResponseEntity<>(shelfService.removeBook(shelf, book), httpStatus);
        }

        if (shelf.getBooks().contains(book)) {
            messages.put("shelf", "Book " + book.getTitle() + " already exists in shelf " + shelf.getName());
            return new ResponseEntity<>(messages, httpStatus);
        }

        if (book.getStatus().equals(Book.SHELVED)) {
            messages.put("book", "Book " + book.getTitle() + " is already shelved in shelf " + book.getShelf().getName());
            return new ResponseEntity<>(messages, httpStatus);
        }

        return new ResponseEntity<>(shelfService.addBook(shelf, book), httpStatus);
    }
}
