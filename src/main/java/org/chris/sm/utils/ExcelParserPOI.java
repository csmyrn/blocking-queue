package org.chris.sm.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelParserPOI implements IteratorParser 
{
	private DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	private List<ParserField> parserFields;
	private boolean hasHeader = false;
	private String sheetName;
	private String file;
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
	public Iterator<Map<String, Object>> parseInput(final InputStream in) throws FileParserException 
	{
		Workbook workbook;
		try {
			workbook = getWorkbook(in);
		} catch (Exception e) {
			throw new FileParserException(e);
		}
		
		final Sheet s1 = sheetName == null ? workbook.getSheetAt(0) : workbook.getSheet(sheetName);
		
		totalRows = s1.getLastRowNum();

		if ((totalRows > 0) || (s1.getPhysicalNumberOfRows() > 0)) {
			totalRows++;
		}
		
		return new Iterator<Map<String,Object>>() {
			private Map<String, Object> curRow;
			private int rowNum = 0;	
			
			Iterator<Row> rowIterator = s1.iterator();
			
			@Override
			public boolean hasNext() 
			{
				if(rowIterator.hasNext())
				{
					if (rowNum == 0 && hasHeader) {
						rowIterator.next();
						rowNum++;
						return hasNext();
					}
					curRow = readRow(rowNum++, rowIterator.next());
					return curRow != null;
				}
				
				return false;
			}
			
			private Map<String, Object> readRow(int rowNumber, Row row) 
			{
				List<String> lineFields = new ArrayList<String>();	
				
				Iterator<Cell> cellIter = row.cellIterator();
				
				String cellVal = null;
				
				while(cellIter.hasNext())
				{
					Cell cell = cellIter.next();
					
					switch(cell.getCellType())
					{
						case Cell.CELL_TYPE_STRING:
							cellVal = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_BOOLEAN:
							cellVal = String.valueOf(cell.getBooleanCellValue());
							break;
						case Cell.CELL_TYPE_NUMERIC:
							cellVal = NumberFormat.getInstance(Locale.US).format(cell.getNumericCellValue()).replaceAll(",", "");
							break;
						
					}
					
					if(cellVal != null)
						lineFields.add(cellVal.trim());
				}
				
				/*for(int col=0;col<s1.getColumns();col++)
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
				}*/
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
	
	private Workbook getWorkbook(InputStream inputStream) throws IOException 
	{
	    Workbook workbook = null;
	 
	    if (StringUtils.endsWithIgnoreCase(file, "xlsx")){
	        workbook = new XSSFWorkbook(inputStream);
	    } else if (StringUtils.endsWithIgnoreCase(file, "xls")) {
	        workbook = new HSSFWorkbook(inputStream);
	    } else {
	        throw new IllegalArgumentException("The specified file [" + file + "] is not a valid Excel file");
	    }
	 
	    return workbook;
	}

	public void setDateFormat(String sDateFormat) {
		this.df = new SimpleDateFormat(sDateFormat);
	}
	
	public void setFile(String file)
	{
		this.file = file;
	}

	@Override
	public int getTotalRows()
	{
		return totalRows;
	}
}
