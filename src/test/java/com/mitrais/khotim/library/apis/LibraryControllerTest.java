package com.mitrais.khotim.library.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitrais.khotim.library.domains.Book;
import com.mitrais.khotim.library.domains.Shelf;
import com.mitrais.khotim.library.services.BookService;
import com.mitrais.khotim.library.services.ShelfService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = LibraryController.class, secure = false)
public class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShelfService shelfService;

    @MockBean
    private BookService bookService;

    private Shelf shelf;
    private Shelf shelf2;
    private Book book;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        shelf = new Shelf("Shelf A", 20);
        shelf.setId(1L);

        shelf2 = new Shelf("Shelf B", 35);
        shelf2.setId(2L);

        book = new Book();
        book.setId(1L);
        book.setShelf(shelf);

        mapper = new ObjectMapper();
    }

    @Test
    public void getAll() throws Exception {
        List<Shelf> shelves = Arrays.asList(shelf, shelf2);

        Mockito.when(shelfService.findAll()).thenReturn(shelves);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/libraries").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(mapper.writeValueAsString(shelves)));
    }

    @Test
    public void getOneWhenShelfExists() throws Exception {
        Mockito.when(shelfService.findById(Mockito.anyLong())).thenReturn(Optional.of(shelf));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/libraries/{id}", Mockito.anyLong()).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(mapper.writeValueAsString(shelf))));
    }

    @Test
    public void getOneWhenShelfNotExists() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/libraries/{id}", Mockito.anyLong()).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(isEmptyString()));
    }

    @Test
    public void addBookWhenShelfNotExists() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/libraries/{id}/addBook", shelf.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(book));

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"shelf\":\"There's no shelf found with id " + shelf.getId() + "\"}"));
    }

    @Test
    public void addBookWhenShelfReachedMaxCapacity() throws Exception {
        shelf.setCurrentCapacity(shelf.getMaxCapacity());

        Mockito.when(shelfService.findById(shelf.getId())).thenReturn(Optional.of(shelf));
        Mockito.when(bookService.findById(book.getId())).thenReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/libraries/{id}/addBook", shelf.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(book));

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"shelf\":\"Shelf " + shelf.getName() + " already reached maximum capacity\"}"));
    }

    @Test
    public void addBookWhenBookNotExists() throws Exception {
        Mockito.when(shelfService.findById(shelf.getId())).thenReturn(Optional.of(shelf));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/libraries/{id}/addBook", shelf.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(book));

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"book\":\"There's no book found with id " + book.getId() + "\"}"));
    }

    @Test
    public void addBookWhenBookExistsInShelf() throws Exception {
        shelf.setBooks(Collections.singletonList(book));

        Mockito.when(shelfService.findById(shelf.getId())).thenReturn(Optional.of(shelf));
        Mockito.when(bookService.findById(book.getId())).thenReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/libraries/{id}/addBook", shelf.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(book));

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"shelf\":\"Book " + book.getTitle() + " already exists in shelf " + shelf.getName() + "\"}"));
    }

    @Test
    public void addBookWhenBookIsShelved() throws Exception {
        book.setStatus(Book.SHELVED);

        Mockito.when(shelfService.findById(shelf.getId())).thenReturn(Optional.of(shelf));
        Mockito.when(bookService.findById(book.getId())).thenReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/libraries/{id}/addBook", shelf.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(book));

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"book\":\"Book " + book.getTitle() + " is already shelved in shelf " + book.getShelf().getName() + "\"}"));
    }

    @Test
    public void addBookSuccess() throws Exception {
        Mockito.when(shelfService.findById(shelf.getId())).thenReturn(Optional.of(shelf));
        Mockito.when(bookService.findById(book.getId())).thenReturn(Optional.of(book));

        shelf2.setBooks(Collections.singletonList(book));
        Mockito.when(shelfService.addBook(shelf, book)).thenReturn(shelf2);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/libraries/{id}/addBook", shelf.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(book));

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(mapper.writeValueAsString(shelf2))));

        assertTrue(shelf2.getBooks().contains(book));
        assertEquals(1, shelf2.getBooks().size());
    }

    @Test
    public void removeBookWhenShelfNotExists() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/api/libraries/{id}/removeBook", shelf.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(book));

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"shelf\":\"There's no shelf found with id " + shelf.getId() + "\"}"));
    }

    @Test
    public void removeBookWhenBookNotExists() throws Exception {
        Mockito.when(shelfService.findById(shelf.getId())).thenReturn(Optional.of(shelf));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/api/libraries/{id}/removeBook", shelf.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(book));

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"book\":\"There's no book found with id " + book.getId() + "\"}"));
    }

    @Test
    public void removeBookWhenBookNotInShelf() throws Exception {
        Mockito.when(shelfService.findById(shelf.getId())).thenReturn(Optional.of(shelf));
        Mockito.when(bookService.findById(book.getId())).thenReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/api/libraries/{id}/removeBook", shelf.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(book));

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"shelf\":\"There's no book " + book.getTitle() + " in shelf " + shelf.getName() + "\"}"));
    }

    @Test
    public void removeBookSuccess() throws Exception {
        shelf.setBooks(Collections.singletonList(book));

        Mockito.when(shelfService.findById(shelf.getId())).thenReturn(Optional.of(shelf));
        Mockito.when(bookService.findById(book.getId())).thenReturn(Optional.of(book));

        Mockito.when(shelfService.removeBook(shelf, book)).thenReturn(shelf2);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/api/libraries/{id}/removeBook", shelf.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(book));

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(mapper.writeValueAsString(shelf2))));

        assertFalse(shelf2.getBooks().contains(book));
        assertEquals(0, shelf2.getBooks().size());
    }
}