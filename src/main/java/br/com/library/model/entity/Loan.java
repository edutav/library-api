package br.com.library.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

	private Long id;
	private String customer;
	private Book book;
	private LocalDate loanDate;
	private Boolean returned;

}
