package br.com.library.services.impl;

import br.com.library.exceptions.BusinessException;
import br.com.library.model.entity.Book;
import br.com.library.model.repositories.BookRepository;
import br.com.library.services.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {
	private BookRepository repository;

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
}
