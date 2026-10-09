package com.ecommerce.tests;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.io.FileInputStream;
import java.io.IOException;

public class TestDataReading {

    public static final String TEST_DATA_FILE_PATH = "src/test/resources/testdata/test_data.xls";
    private static final DataFormatter FORMATTER = new DataFormatter();

    public static void main(String[] args) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(TEST_DATA_FILE_PATH);
             HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream)) {

            HSSFSheet sheet = workbook.getSheet("Sheet1");
            int lastRowNum = sheet.getLastRowNum();
            System.out.println("Last Row Number: " + lastRowNum);

            int lastCellNum = sheet.getRow(0).getLastCellNum();
            System.out.println("Last Cell Number: " + lastCellNum);

            for (int i = 0; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < lastCellNum; j++) {
                    line.append(getCellData(row, j)).append('\t');
                }
                System.out.println(line);
            }

            System.out.println("Cell(2,0): " + getCellData(sheet.getRow(2), 0));
        }
    }

    private static String getCellData(Row row, int colNum) {
        return FORMATTER.formatCellValue(row.getCell(colNum));
    }
}


