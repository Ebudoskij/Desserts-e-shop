package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.dto.ImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CategoryImportService {

    /**
     * Parses an .xlsx file and bulk-imports Category entities.
     * Column layout (row 0 = header, skipped):
     *   A=name, B=description, C=parentName (blank → root)
     */
    ImportResult importFromXlsx(MultipartFile file) throws IOException;

    /**
     * Generates a downloadable .xlsx template with a bold header row
     * and a few sample rows demonstrating the expected format.
     */
    byte[] buildTemplate() throws IOException;
}
