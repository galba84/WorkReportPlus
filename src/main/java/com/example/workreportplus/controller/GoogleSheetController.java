package com.example.workreportplus.controller;

import com.example.workreportplus.service.GoogleSheetsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
public class GoogleSheetController {

    private final GoogleSheetsService sheetsService;

    public GoogleSheetController(GoogleSheetsService sheetsService) {
        this.sheetsService = sheetsService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/read-sheets")
    public List<List<Object>> readSheets(@RequestParam String sheetId,
                                         @RequestParam String upperBound, @RequestParam String lowerBound)
            throws IOException, GeneralSecurityException {

        String range = "ID!A1:B20"; // <-- adjust to your sheet range
        range = sheetId+upperBound+lowerBound;
        List<List<Object>> data = sheetsService.readSheet(GoogleSheetsService.SHPS_TABLE_ID, range);

        if (data == null || data.isEmpty()) {
            return List.of();
        }

        // Simple string representation
        return data;
    }
}
