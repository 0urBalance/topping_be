package org.balanceus.topping.infrastructure.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorData {
	private Integer code;
	private String message;
	private String details;
}