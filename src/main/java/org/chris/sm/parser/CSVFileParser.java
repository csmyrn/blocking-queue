package org.chris.sm.parser;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.chris.sm.utils.*;

import java.io.*;
import java.util.*;


public class CSVFileParser implements RWFileParser, IteratorParser
{
	private List<ParserField> parserFields;
	private String separator = ";";
	private String lineFeed = null;
	private boolean hasHeader = false;
	private boolean ignoreSeperatorInsideDoubleQuotes = false;
	private String characterSet;
	private static String DOUBLE_QUOTE = "DOUBLE_QUOTE";
	private Map<InputStream, BufferedReader> readerMap = new HashMap<InputStream, BufferedReader>();
	private int totalLines = -1; //updated after parsing
	private static final Logger log = LogManager.getLogger(CSVFileParser.class);
	
	public List<Map<String, Object>> parse(InputStream in) throws FileParserException
	{	
		//Assert.hasText(characterSet, "please define characterSet property");
		try 
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(in, characterSet));
			
			log.info("parsing file..");
			String line;
			String[] fields = null;
			List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
			int lineNum = 0;			
			
			while ((line = readLine(r)) != null) 
			{
				try {
					if (lineNum == 0 && hasHeader) {
						lineNum++;
						continue;					
					}
					if (!StringUtils.isNotBlank(line) || line.startsWith("#"))
						continue;
					if(!ignoreSeperatorInsideDoubleQuotes)
						fields = line.replaceAll("\"", "").split(separator, -1);
					else
						fields = line.split(separator+"(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
					result.add(ParserUtils.parseLine(fields, parserFields));
				}
				catch (Exception e) {
					log.info("Error parsing line [" + lineNum + "]\nRaw: " + line + "\nParsed: " + Arrays.toString(fields) + "\n" + e);
				}

				if(lineNum++ % 20000 == 0)
					log.info("parsed " + lineNum + " lines so far..");
			}
			
			log.info("parsed [OK], lines [" + (totalLines = result.size()) + "]");
			in.close();
			return result;
		} 
		catch (Exception e) {
			throw new FileParserException(e);
		}
	}
	
	private String readLine(BufferedReader r) throws IOException {
		String line = readLineInternal(r);
		if (line == null)
			return null;
		if(!ignoreSeperatorInsideDoubleQuotes)
			line = line.replaceAll("(\r\n){2,10}", "\r\n").replaceAll("(\n\n){2,10}", "\n").replaceAll("\"","");
		return line;

	}
	
	private String readLineInternal(BufferedReader r) throws IOException 
	{		
		if (lineFeed == null)
			return r.readLine();
		else if (lineFeed.equals(DOUBLE_QUOTE)) {			
			String line = r.readLine();
			if (line == null)
			return null;
			if (line.endsWith("\""))
				return line;
			else {
				StringBuffer sb = new StringBuffer();
				for (;;) {
					sb.append(line).append("\r\n");
					if (line == null || line.endsWith("\""))
						return sb.toString();					
					line = r.readLine();
				}
			}
		} 
		else 
			throw new RuntimeException("line feed [" + lineFeed + "] unknown");
	}
	
	public Map<Object, Map<String, Object>> parse(String keyField, InputStream in) throws FileParserException 
	{
		//Assert.hasText(characterSet, "please define characterSet property");
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(in, characterSet));
			String line;
			HashMap<Object, Map<String, Object>> result = new HashMap<Object, Map<String,Object>>();
			int lineNum = 0;
			log.debug("parsing file using keyField [" + keyField + "]..");
			while ((line = readLine(r)) != null) {
				lineNum++;
				if (lineNum == 0 && hasHeader)					
					continue;									
				if (!StringUtils.isNotBlank(line))
					continue;
				String[] fields = line.replaceAll("\"", "").split(separator);
				Map<String, Object> row = ParserUtils.parseLine(fields, parserFields);
				if (row.get(keyField) == null)
					throw new FileParserException("row [" + lineNum +"] does not contain field [" + keyField + "]");
				result.put(row.get(keyField), row);
				
				if(lineNum % 20000 == 0)
					log.info("parsed " + lineNum + " lines so far..");
			}
			log.debug("parsed [OK], lines [" + (totalLines = result.size()) + "]");
			return result;
		} catch (Exception e) {
			throw new FileParserException(e);
		}
	}
	
	public String save(Map<String, Object> row, boolean isFirstRow) throws FileParserException {
		StringBuffer line = new StringBuffer();
		if (hasHeader && isFirstRow) {
			int i = 1;
			for (ParserField f: parserFields) { 
				line.append(f.getName());
				if (i < parserFields.size())
					line.append(separator);
			}
			line.append("\n");
		}

		int i = 1;
		for (ParserField f: parserFields) {
			if (f.getConstantValue() != null)
				line.append(ParserUtils.fromObjectToString(f, f.getConstantValue()));
			else
				line.append(ParserUtils.fromObjectToString(f, row.get(f.getName())));
			if (i < parserFields.size())
				line.append(separator);
			i++;
		}
		line.append("\n");
		return line.toString();		
	}
	
	public String save(Collection<Map<String, Object>> data) throws FileParserException 
	{
		StringWriter writer = new StringWriter();
		if (hasHeader) {
			StringBuffer line = new StringBuffer();
			int i = 1;
			for (ParserField f: parserFields) { 
				line.append(f.getName());
				if (i < parserFields.size())
					line.append(separator);
				i++;
			}
			line.append("\n");
			writer.write(line.toString());
		}
		for (Map<String, Object> row: data) {
			StringBuffer line = new StringBuffer();
			int i = 1;
			for (ParserField f: parserFields) {
				if (f.getConstantValue() != null)
					line.append(ParserUtils.fromObjectToString(f, f.getConstantValue()));
				else
					line.append(ParserUtils.fromObjectToString(f, row.get(f.getName())));
				if (i < parserFields.size())
					line.append(separator);
				i++;

			}
			line.append("\n");
			writer.write(line.toString());
		}
		return writer.toString();
	}
	
	public synchronized Map<String, Object> nextRow(InputStream in) throws FileParserException 
	{
		BufferedReader r;
		String line;
		try {
			if (readerMap.get(in) == null) {
				//Assert.hasText(characterSet, "please define characterSet property");
				r = new BufferedReader(new InputStreamReader(in, characterSet));
				readerMap.put(in, r);
				if (hasHeader)
					line = readLine(r);			
			} else
				r = readerMap.get(in);
			if (r == null)
				return null;
			line = r.readLine();
			while (line != null && !StringUtils.isNotBlank(line))
				line = readLine(r);
			if (line == null)
				return null;				
			String[] fields = line.replaceAll("\"", "").split(separator);
			return ParserUtils.parseLine(fields, parserFields);
		} catch (Exception e) {
			throw new FileParserException(e.getMessage());
		}
	}
	
	public Iterator<Map<String, Object>> parseInput(InputStream in) throws FileParserException {
		return (Iterator<Map<String, Object>>) this.parse(in).iterator();
	}
	
	public void setLineFeed(String lineFeed) {
		this.lineFeed = lineFeed;
	}
	
	public void setCharacterSet(String characterSet) {
		this.characterSet = characterSet;
	}
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	
	public void setIgnoreSeperatorInsideDoubleQuotes(boolean ignoreSeperatorInsideDoubleQuotes) {
		this.ignoreSeperatorInsideDoubleQuotes = ignoreSeperatorInsideDoubleQuotes;
	}
	 
	public void setParserFields(List<ParserField> fields) {
		this.parserFields = fields;
	}
	
	public void setHasHeader(boolean hasHeader) {
		this.hasHeader = hasHeader;
	}

	@Override
	public int getTotalRows()
	{
		return totalLines;
	}
}
