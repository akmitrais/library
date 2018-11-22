package com.mitrais.khotim.library.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitrais.khotim.library.domains.Book;
import com.mitrais.khotim.library.services.BookService;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.isEmptyString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = BookController.class, secure = false)
public class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    private Book book;
    private Book book2;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        book = new Book("9876", "Space 1", "Khotim");
        book.setId(1L);

        book2 = new Book("1234", "Space 2", "Khotim");
        book2.setId(2L);

        mapper = new ObjectMapper();
    }

    @Test
    public void getAll() throws Exception {
        List<Book> books = Arrays.asList(book, book2);

        Mockito.when(bookService.findByTitleAndStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(books);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(mapper.writeValueAsString(books)));
    }

    @Test
    public void getOneWhenBookExists() throws Exception {
        Mockito.when(bookService.findById(book.getId())).thenReturn(Optional.ofNullable(book));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", book.getId()).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(mapper.writeValueAsString(book)));
    }

    @Test
    public void getOneWhenBookNotExists() throws Exception {
        Mockito.when(bookService.findById(book.getId())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", book.getId()).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(isEmptyString()));
    }
}