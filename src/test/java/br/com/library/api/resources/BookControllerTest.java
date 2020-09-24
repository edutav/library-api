package br.com.library.api.resources;

import br.com.library.api.dtos.BookDTO;
import br.com.library.exceptions.BusinessException;
import br.com.library.model.entity.Book;
import br.com.library.services.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

	static String BOOK_API = "/api/books";

	@Autowired
	MockMvc mvc;

	@MockBean
	BookService bookService;

	@Test
	@DisplayName("Deve criar um livro.")
	public void createBook() throws Exception {
		BookDTO dto = createNewBook();

		Book book = Book.builder().id(10L).author("Eduardo").title("As Aventuras").isbn("147852").build();

		BDDMockito.given(bookService.save(Mockito.any(Book.class)))
				.willReturn(book);

		String json = new ObjectMapper().writeValueAsString(dto);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("id").value(10L))
				.andExpect(jsonPath("title").value(dto.getTitle()))
				.andExpect(jsonPath("author").value(dto.getAuthor()))
				.andExpect(jsonPath("isbn").value(dto.getIsbn()));
	}

	@Test
	@DisplayName("Deve lançar erro de validação quando não houver dados suficientes para a criação livro.")
	public void createInvalidBook() throws Exception {
		String json = new ObjectMapper().writeValueAsString(new BookDTO());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("errors", Matchers.hasSize(3)));
	}

	@Test
	@DisplayName("Deve lançar erro ao tentar cadastrar um livro com isbn já cadastrado.")
	public void createBookWithDuplicateIsbn() throws Exception {
		BookDTO dto = createNewBook();
		String json = new ObjectMapper().writeValueAsString(dto);

		String mensagemErro = "ISBN já cadastrado.";
		BDDMockito.given(bookService.save(Mockito.any(Book.class))).willThrow(new BusinessException(mensagemErro));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request).andExpect(status().isBadRequest())
				.andExpect(jsonPath("errors",Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value(mensagemErro));
	}

	private BookDTO createNewBook() {
		return BookDTO.builder().author("Eduardo").title("As Aventuras").isbn("147852").build();
	}

}
