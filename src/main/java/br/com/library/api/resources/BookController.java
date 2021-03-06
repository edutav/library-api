package br.com.library.api.resources;

import br.com.library.api.dtos.BookDTO;
import br.com.library.api.exception.ApiErrors;
import br.com.library.exceptions.BusinessException;
import br.com.library.model.entity.Book;
import br.com.library.services.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

	private final BookService bookService;
	private final ModelMapper modelMapper;

	public BookController(BookService bookService, ModelMapper modelMapper) {
		this.bookService = bookService;
		this.modelMapper = modelMapper;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookDTO create(@RequestBody @Valid BookDTO dto) {
		Book book = modelMapper.map(dto, Book.class);
		book = bookService.save(book);
		return modelMapper.map(book, BookDTO.class);
	}

	@GetMapping("{id}")
	@ResponseStatus(HttpStatus.OK)
	public BookDTO get(@PathVariable Long id) {
		return bookService.getById(id).map(book -> modelMapper.map(book, BookDTO.class))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		Book book = bookService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		bookService.delete(book);
	}

	@PutMapping("{id}")
	public BookDTO update(@PathVariable Long id, BookDTO dto) {
		return bookService.getById(id).map(book -> {
			book.setAuthor(dto.getAuthor());
			book.setTitle(dto.getTitle());
			book = bookService.update(book);
			return modelMapper.map(book, BookDTO.class);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@GetMapping
	public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
		Book filter = modelMapper.map(dto, Book.class);
		Page<Book> result = bookService.find(filter, pageRequest);
		List<BookDTO> list = result.getContent()
				.stream()
				.map(entity -> modelMapper.map(entity, BookDTO.class))
				.collect(Collectors.toList());

		return new PageImpl<BookDTO>(list, pageRequest, result.getTotalElements());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrors handleValidationExceptions(MethodArgumentNotValidException exception) {
		BindingResult bindResult = exception.getBindingResult();
		return new ApiErrors(bindResult);
	}

	@ExceptionHandler(BusinessException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrors handleBusinessException(BusinessException exception) {
		return new ApiErrors(exception);
	}

}
