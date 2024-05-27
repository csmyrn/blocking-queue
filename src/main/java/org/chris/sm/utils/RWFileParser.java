package org.chris.sm.utils;

import java.util.Collection;
import java.util.Map;

public interface RWFileParser extends FileParser {
	public String save(Collection<Map<String, Object>> data) throws FileParserException;
	public String save(Map<String, Object> linedata, boolean isFirstRow) throws FileParserException;
}
