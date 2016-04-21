package com.risevision.dap;

public enum QueueName {

	DEFAULT("default");

	private String name;

	QueueName (final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
