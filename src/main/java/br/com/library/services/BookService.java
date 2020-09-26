package br.com.library.services;

import br.com.library.model.entity.Book;

import java.util.Optional;

public interface BookService {
	Book save(Book book);

	Optional<Book> getById(Long id);

	void delete(Book book);

	Book update(Book book);
}
