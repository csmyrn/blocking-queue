package org.chris.sm.utils;

public class FileParserException extends Exception {
	private static final long serialVersionUID = -3053712045139328147L;

	public FileParserException(Exception e) {
		super(e);
	}
	public FileParserException(String msg) {
		super(msg);
	}
}
