package br.com.library.model.repositories;

import br.com.library.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

	@Autowired
	TestEntityManager entityManager;

	@Autowired
	BookRepository repository;

	@Test
	@DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado.")
	public void returnTrueWhenIsbnExists() {
		//cenário
		String isbn = "123";
		Book book = createBook();

		entityManager.persist(book);

		//execução
		boolean exists = repository.existsByIsbn(isbn);

		//verificação
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("Deve retornar falso quando não existir um livro na base com o isbn informado.")
	public void returnFalseWhenIsbnDoesntExist() {
		//cenário
		String isbn = "123";

		//execução
		boolean exists = repository.existsByIsbn(isbn);

		//verificação
		assertThat(exists).isFalse();
	}

	@Test
	@DisplayName("Deve obter um livro por id.")
	public void findByIdTest() {
		//cenário
		Book book = createBook();
		entityManager.persist(book);

		//execução
		Optional<Book> foundBook = repository.findById(book.getId());

		//verificação
		assertThat(foundBook.isPresent()).isTrue();
	}

	private Book createBook() {
		return Book.builder().author("Eduardo").title("Titulo").isbn("123").build();
	}

}
