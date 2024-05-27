package org.chris.sm.utils;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface FileParser {
	public List<Map<String, Object>> parse(InputStream in) throws FileParserException;
	public Map<String, Object> nextRow(InputStream in) throws FileParserException;
}
