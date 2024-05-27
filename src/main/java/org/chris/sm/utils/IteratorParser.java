package org.chris.sm.utils;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

public interface IteratorParser {
	public Iterator<Map<String, Object>> parseInput(InputStream in) throws FileParserException;
	public int getTotalRows();
}
