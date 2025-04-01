package com.example.workreportplus.controller;

import com.example.workreportplus.service.WordTemplateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/daily-work-report")
public class ExportReportController {

    private final WordTemplateService wordTemplateService;

    public ExportReportController(WordTemplateService wordTemplateService) {
        this.wordTemplateService = wordTemplateService;
    }

    @GetMapping("/export")
    public String showExportReportPage(Model model) {
        // List available templates
        List<String> templates = List.of("Group Report", "Region Report");
        List<String> regionNames = List.of("Group Report", "Region Report");
        List<String> groupNames = List.of("Group Report", "Region Report");

        // Add templates to the model for the dropdown selection
        model.addAttribute("templates", templates);
        model.addAttribute("regionNames", templates);
        model.addAttribute("groupNames", templates);

        return "exportReport"; // This will render exportReport.html
    }

    @GetMapping("/export/word")
    public ResponseEntity<byte[]> exportWord(@RequestParam String template) throws IOException {
        // Define placeholders and their values
        Map<String, String> variables = new HashMap<>();
        if ("invoice".equalsIgnoreCase(template)) {
            variables.put("name", "John Doe");
            variables.put("orderNumber", "INV-2024-001");
            variables.put("company", "Acme Corp");
        } else if ("contract".equalsIgnoreCase(template)) {
            variables.put("name", "Jane Smith");
            variables.put("contractId", "CNT-2024-123");
            variables.put("company", "Tech Solutions");
        } else if ("report".equalsIgnoreCase(template)) {
            variables.put("name", "Alex Johnson");
            variables.put("reportDate", "March 10, 2025");
            variables.put("company", "Global Analytics");
        } else {
            return ResponseEntity.badRequest().body(null);
        }

        byte[] wordBytes = wordTemplateService.generateWordFromRtfTemplate(template, variables);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + template + ".rtf")
                .contentType(MediaType.valueOf("application/rtf"))
                .body(wordBytes);
    }
}
