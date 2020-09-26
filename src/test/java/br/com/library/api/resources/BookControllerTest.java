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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

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

	@Test
	@DisplayName("Deve obter informações de um livro.")
	public void getBookDetailsTest() throws Exception {
		//cenário (given - dado) no BDD
		Long id = 1L;
		Book book = Book.builder()
				.id(id)
				.isbn(createNewBook().getIsbn())
				.title(createNewBook().getTitle())
				.author(createNewBook().getAuthor())
				.build() ;
		BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

		//execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat("/"+id)).accept(MediaType.APPLICATION_JSON);
		mvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(jsonPath("id").value(id))
				.andExpect(jsonPath("title").value(createNewBook().getTitle()))
				.andExpect(jsonPath("author").value(createNewBook().getAuthor()))
				.andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));
	}

	@Test
	@DisplayName("Deve retornar resource not found quando o livro procurado não existir.")
	public void bookNotFoundTest() throws Exception {
		//cenário (given - dado) no BDD

		BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

		//execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat("/"+1)).accept(MediaType.APPLICATION_JSON);
		mvc.perform(request)
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Deve deletar um livro.")
	public void deleteBookTest() throws Exception {
		//cenário (given - dado) no BDD
		BDDMockito.given(bookService.getById(Mockito.anyLong()))
				.willReturn(Optional.of(Book.builder().id(1L).build()));

		//execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/"+1));

		mvc.perform(request).andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("Deve retornar resource not found quando não encontrar o livro para deletar.")
	public void deleteInexistentBookTest() throws Exception {
		//cenário (given - dado) no BDD
		BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

		//execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/"+1));

		mvc.perform(request).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Deve deve atualizar um livro.")
	public void updateBookTest() throws Exception {
		//cenário (given - dado) no BDD
		Long id = 1L;
		String json = new ObjectMapper().writeValueAsString(createNewBook());

		Book update = Book.builder().id(1L).title("Mais um").author("Tavares").isbn("147852").build();
		BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(update));

		Book updatedBook = Book.builder().id(id).author("Eduardo").title("As Aventuras").isbn("147852").build();
		BDDMockito.given(bookService.update(update)).willReturn(updatedBook);

		//execução
		MockHttpServletRequestBuilder request =
				MockMvcRequestBuilders.put(BOOK_API.concat("/"+1))
						.content(json)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isOk())
				.andExpect(jsonPath("id").value(id))
				.andExpect(jsonPath("title").value(createNewBook().getTitle()))
				.andExpect(jsonPath("author").value(createNewBook().getAuthor()))
				.andExpect(jsonPath("isbn").value("147852"));
	}

	@Test
	@DisplayName("Deve deve retornar 404 ao tentar atualizar um livro inexistente.")
	public void updateInexistentBookTest() throws Exception {
		//cenário (given - dado) no BDD
		String json = new ObjectMapper().writeValueAsString(createNewBook());

		BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

		//execução
		MockHttpServletRequestBuilder request =
				MockMvcRequestBuilders.put(BOOK_API.concat("/"+1))
						.content(json)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Deve filtrar livros.")
	public void findBooksTest() throws Exception {
		Long id = 1L;

		Book book = Book.builder()
						.id(id)
						.author(createNewBook().getAuthor())
						.title(createNewBook().getTitle())
						.isbn(createNewBook().getIsbn())
						.build();

		//execução
		BDDMockito.given(bookService.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
				.willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

		String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

		MockHttpServletRequestBuilder request =
				MockMvcRequestBuilders.get(BOOK_API.concat(queryString))
						.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(jsonPath("content", Matchers.hasSize(1)))
				.andExpect(jsonPath("totalElements").value(1))
				.andExpect(jsonPath("pageable.pageSize").value(100))
				.andExpect(jsonPath("pageable.pageNumber").value(0));
	}

	private BookDTO createNewBook() {
		return BookDTO.builder().author("Eduardo").title("As Aventuras").isbn("147852").build();
	}

}
