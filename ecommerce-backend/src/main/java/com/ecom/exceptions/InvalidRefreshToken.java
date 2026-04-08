package com.ecom.exceptions;

public class InvalidRefreshToken extends RuntimeException {
	public InvalidRefreshToken(String message) {
		super(message);
	}
}
