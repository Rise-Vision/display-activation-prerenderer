package com.risevision.dap;

public class BQException extends Exception {

	private static final long serialVersionUID = 3105041282538565115L;

	public BQException(String message) {
		super(message);
	}

	public BQException(Throwable cause) {
		super(cause);
	}

	public BQException(String message, Throwable cause) {
		super(message, cause);
	}

	public BQException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}


}
