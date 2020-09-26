package br.com.library.services;

import br.com.library.exceptions.BusinessException;
import br.com.library.model.entity.Book;
import br.com.library.model.repositories.BookRepository;
import br.com.library.services.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

	BookService bookService;

	@MockBean
	BookRepository repository;

	@BeforeEach
	public void setUp() {
		this.bookService = new BookServiceImpl(repository);
	}


	@Test
	@DisplayName("Deve savar um livro.")
	public void saveBookTest() {
		//cenário
		Book book = createValidBook();
		Mockito.when( repository.existsByIsbn(Mockito.anyString()) ).thenReturn(false);

		Mockito.when(repository.save(book)).thenReturn(Book.builder()
				.id(1L)
				.isbn("1234")
				.title("Teste")
				.author("Fulano")
				.build());

		//execução
		Book savedBook = bookService.save(book);

		//verificação
		assertThat(savedBook.getId()).isNotNull();
		assertThat(savedBook.getIsbn()).isEqualTo("1234");
		assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
		assertThat(savedBook.getTitle()).isEqualTo("Teste");
	}

	private Book createValidBook() {
		return Book.builder().isbn("1234").title("Teste").author("Fulano").build();
	}

	@Test
	@DisplayName("Deve lançar erro de negocio ao tentar salvar um livbro com isbn duplicado")
	public void shouldNotSaveABookWithDuplicatedISBN() {
		//cenário
		Book book = createValidBook();
		Mockito.when( repository.existsByIsbn(Mockito.anyString()) ).thenReturn(true);

		//execução
		Throwable exception = Assertions.catchThrowable(() -> bookService.save(book));

		//verificações
		assertThat(exception)
				.isInstanceOf(BusinessException.class)
				.hasMessage("ISBN já cadastrado.");

		Mockito.verify(repository, Mockito.never()).save(book);
	}

	@Test
	@DisplayName("Deve obter um livro por id")
	public void getByIdTest() {
		//cenário
		Long id = 1L;
		Book book = createValidBook();
		book.setId(id);
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

		//execução
		Optional<Book> foundBook = bookService.getById(id);

		//verificações
		assertThat(foundBook.isPresent()).isTrue();
		assertThat(foundBook.get().getId()).isEqualTo(id);
		assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
		assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
		assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());

	}

	@Test
	@DisplayName("Deve retornar vazio ao obter um livro por id quando ele não existir na base.")
	public void bookNotFoundByIdTest() {
		//cenário
		Long id = 1L;
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

		//execução
		Optional<Book> book = bookService.getById(id);

		//verificações
		assertThat(book.isPresent()).isFalse();
	}

}
