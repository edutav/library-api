package br.com.library.services.impl;

import br.com.library.exceptions.BusinessException;
import br.com.library.model.entity.Book;
import br.com.library.model.repositories.BookRepository;
import br.com.library.services.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {
	private final BookRepository repository;

	public BookServiceImpl(BookRepository repository) {
		this.repository = repository;
	}

	@Override
	public Book save(Book book) {
		if (repository.existsByIsbn(book.getIsbn())) {
			throw new BusinessException("ISBN já cadastrado.");
		}
		return repository.save(book);
	}

	@Override
	public Optional<Book> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public void delete(Book book) {
		if (book == null || book.getId() == null) {
			throw new IllegalArgumentException("Book não pode estar nulo");
		}
		this.repository.delete(book);
	}

	@Override
	public Book update(Book book) {
		if (book == null || book.getId() == null) {
			throw new IllegalArgumentException("Book não pode estar nulo");
		}
		return this.repository.save(book);
	}

	@Override
	public Page<Book> find(Book filter, Pageable pageRequest) {
		Example<Book> example = Example.of(filter, ExampleMatcher.matching()
				.withIgnoreCase().withIgnoreNullValues().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
		return repository.findAll(example, pageRequest);
	}
}
