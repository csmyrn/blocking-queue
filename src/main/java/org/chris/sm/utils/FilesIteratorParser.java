package org.chris.sm.utils;

import java.io.File;
import java.util.Iterator;

public interface FilesIteratorParser {
	//returns an iterator for <hotelCode> for files contained in filesPath
	public Iterator<String> parseFilesList(File filesPath) throws FileParserException;
}