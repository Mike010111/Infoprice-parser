package by.infoprice.parser.export;

import by.infoprice.parser.model.ProductRow;
import by.infoprice.parser.model.ProductRow.PriceInfo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    public void export(List<ProductRow> products, String filePath) throws IOException {
        byte[] bytes = exportToBytes(products);
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            fileOut.write(bytes);
        }
    }

    public byte[] exportToBytes(List<ProductRow> products) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Акционные товары");

            CellStyle headerStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);

            CellStyle priceStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            priceStyle.setDataFormat(format.getFormat("0.00"));

            CellStyle promoStyle = workbook.createCellStyle();
            promoStyle.setDataFormat(format.getFormat("0.00"));
            Font redFont = workbook.createFont();
            redFont.setColor(IndexedColors.RED.getIndex());
            redFont.setBold(true);
            promoStyle.setFont(redFont);

            Row headerRow = sheet.createRow(0);
            Cell firstHeaderCell = headerRow.createCell(0);
            firstHeaderCell.setCellValue("Название товара");
            firstHeaderCell.setCellStyle(headerStyle);

            List<String> storeNames = products.isEmpty()
                    ? List.of()
                    : List.copyOf(products.get(0).getPricesByStore().keySet());

            for (int i = 0; i < storeNames.size(); i++) {
                Cell headerCell = headerRow.createCell(i + 1);
                headerCell.setCellValue(storeNames.get(i));
                headerCell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (ProductRow product : products) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(product.getProductName());

                Map<String, PriceInfo> prices = product.getPricesByStore();
                for (int i = 0; i < storeNames.size(); i++) {
                    PriceInfo info = prices.get(storeNames.get(i));
                    Cell cell = row.createCell(i + 1);
                    writePriceCell(cell, info, priceStyle, promoStyle);
                }
            }

            for (int i = 0; i <= storeNames.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void writePriceCell(Cell cell, PriceInfo info, CellStyle priceStyle, CellStyle promoStyle) {
        if (info == null || info.getPrice() == null || info.getPrice().isBlank() || info.getPrice().equals("-")) {
            cell.setCellValue("-");
            return;
        }

        String normalized = info.getPrice().trim().replace(",", ".");

        try {
            double numericValue = Double.parseDouble(normalized);
            cell.setCellValue(numericValue);
            cell.setCellStyle(info.isPromotional() ? promoStyle : priceStyle);
        } catch (NumberFormatException e) {
            cell.setCellValue(info.getPrice());
        }
    }
}
