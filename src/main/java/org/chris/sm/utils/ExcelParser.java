package org.chris.sm.utils;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jxl.Cell;
import jxl.DateCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;

public class ExcelParser implements IteratorParser {
	private DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	private List<ParserField> parserFields;
	private boolean hasHeader = false;
	private String sheetName;
	private int totalRows = -1; //updated when sheet is read
	
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public void setHasHeader(boolean hasHeader) {
		this.hasHeader = hasHeader;
	}

	public void setParserFields(List<ParserField> parserFields) {
		this.parserFields = parserFields;
	}

	@Override
	public Iterator<Map<String, Object>> parseInput(final InputStream in) throws FileParserException {
		Workbook workbook;
		try {
			workbook = Workbook.getWorkbook(in);
		} catch (Exception e) {
			throw new FileParserException(e);
		}
		
		final Sheet s1 = sheetName == null ? workbook.getSheets()[0] : workbook.getSheet(sheetName);
		
		totalRows = s1.getRows();
		
		return new Iterator<Map<String,Object>>() {
			private Map<String, Object> curRow;
			private int rowNum = 0;			
			
			@Override
			public boolean hasNext() {
				if (s1.getCell(0, 1).getContents() == null || 
						s1.getCell(0, 1).getContents().length() == 0)					
					return false;
				if (rowNum == 0 && hasHeader) {
					rowNum++;
				}				
				curRow = readRow(rowNum++);
				return curRow != null;
			}

			private Map<String, Object> readRow(int rowNumber) {
				if (rowNumber >= s1.getRows())
					return null;
				List<String> lineFields = new ArrayList<String>();	
				for(int col=0;col<s1.getColumns();col++)
				{
					Cell c1=s1.getCell(col,rowNumber);
					String cellVal;
					if (c1 instanceof DateCell && c1.getContents() != null)
						cellVal = df.format(((DateCell) c1).getDate());
					else if (c1 instanceof NumberCell)
						cellVal = NumberFormat.getInstance(Locale.US).format(((NumberCell) c1).getValue()).replaceAll(",", "");
					else
						cellVal = c1.getContents().trim();
					lineFields.add(cellVal);
				}
				try {
					return ParserUtils.parseLine((String[])lineFields.toArray(new String[lineFields.size()]), parserFields);
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}

			}
			@Override
			public Map<String, Object> next() {
				return curRow;
			}

			@Override
			public void remove() {
				throw new RuntimeException("not supported");
			}

		};
	}

	public void setDateFormat(String sDateFormat) {
		this.df = new SimpleDateFormat(sDateFormat);
	}

	@Override
	public int getTotalRows()
	{
		return totalRows;
	}
}
