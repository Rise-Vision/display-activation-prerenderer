package com.risevision.dap;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class QueueTaskImplementation implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract void execute(HttpServletRequest request, HttpServletResponse response) throws Exception;

	// Utilities

	protected Logger getLogger() {

		return Logger.getLogger(this.getClass().getName());
	}
}
