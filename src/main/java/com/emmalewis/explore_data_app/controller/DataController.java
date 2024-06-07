package com.emmalewis.explore_data_app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emmalewis.explore_data_app.service.DataAnalysisService;

@Controller
public class DataController {

    @Autowired
    private DataAnalysisService dataAnalysisService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
                redirectAttributes.addFlashAttribute("error", "File size exceeds the limit of 10MB.");
                return "redirect:/";
            }
            String summary = dataAnalysisService.analyzeData(file);
            List<String> columns = dataAnalysisService.getColumns();
            model.addAttribute("summary", summary);
            model.addAttribute("columns", columns);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to process file: " + e.getMessage());
        }
        return "result";
    }

    @GetMapping("/visualize")
    public String visualize(@RequestParam("column") String column, Model model) {
        String visualizationScript = dataAnalysisService.generateVisualization(column);
        model.addAttribute("column", column);
        model.addAttribute("visualizationScript", visualizationScript);
        return "visualization";
    }
}