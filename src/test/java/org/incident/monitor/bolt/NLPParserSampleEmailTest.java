package org.incident.monitor.bolt;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.incident.monitor.Incident;
import org.junit.Test;

public class NLPParserSampleEmailTest extends NLPParserTest {

	public static void readSheetWithFormula() {
		try {
			FileInputStream file = new FileInputStream(new File("NLPParserTestSheet.xlsx"));

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				// For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();

				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					// Check the cell type after evaluating formulae
					// If it is formula cell, it will be evaluated otherwise no
					// change will happen
					switch (evaluator.evaluateInCell(cell).getCellType()) {
					case Cell.CELL_TYPE_NUMERIC:
						System.out.print(cell.getNumericCellValue() + "tt");
						break;
					case Cell.CELL_TYPE_STRING:
						System.out.print(cell.getStringCellValue() + "tt");
						break;
					case Cell.CELL_TYPE_FORMULA:
						// Not again
						break;
					}
				}
				System.out.println("");
			}
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testExcelSheetSamples() {
		List<Incident> i = checkGetIncidents("Power outage in Singapore on 20 October 2011", "");
		assertEquals(1, i.size());
		assertEquals("20 October 2011", i.get(0).getDate());
	}

}
