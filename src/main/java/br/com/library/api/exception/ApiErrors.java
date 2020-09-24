package br.com.library.api.exception;

import br.com.library.exceptions.BusinessException;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErrors {

	private List<String> errors;

	public ApiErrors(BindingResult bindResult) {
		this.errors = new ArrayList<>();
		bindResult.getAllErrors().forEach(error -> this.errors.add(error.getDefaultMessage()));
	}

	public ApiErrors(BusinessException exception) {
		this.errors = Arrays.asList(exception.getMessage());
	}

	public List<String> getErrors() {
		return errors;
	}
}
