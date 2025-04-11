package com.example.workreportplus.controller;

import com.example.workreportplus.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ContractorService contractorService;
    private final GroupService groupService;
    private final RegionService regionService;
    private final DescriptionTemplateService descriptionTemplateService;
    private final PlacesService placesService;

    public AdminController(ContractorService contractorService, GroupService groupService,
                           RegionService regionService, DescriptionTemplateService descriptionTemplateService, PlacesService placesService) {
        this.contractorService = contractorService;
        this.groupService = groupService;
        this.regionService = regionService;
        this.descriptionTemplateService = descriptionTemplateService;
        this.placesService = placesService;
    }

    @GetMapping
    public String admin(Model model) {
        return "admin";
    }

    @PostMapping("/contractors")
    public String updateContractorsFromTable(RedirectAttributes redirectAttributes) throws IOException {
        contractorService.updateContractorsFromTable();
        redirectAttributes.addFlashAttribute("infoMessage", "Contractors updated successfully!");
        return "redirect:/admin"; // return to admin page after update
    }

    @PostMapping("/groups")
    public String updateGroupsFromTable(RedirectAttributes redirectAttributes) throws IOException {
        groupService.updateGroupsFromTable();
        redirectAttributes.addFlashAttribute("infoMessage", "Groups updated successfully!");
        return "redirect:/admin"; // return to admin page after update
    }


    @PostMapping("/regions")
    public String updateRegionsFromTable(RedirectAttributes redirectAttributes) throws IOException {
        regionService.updateRegionsFromTable();
        redirectAttributes.addFlashAttribute("infoMessage", "Regions updated successfully!");
        return "redirect:/admin"; // redirect with flash message
    }

    @PostMapping("/groups/descriptions")
    public String updateGroupDescription(RedirectAttributes redirectAttributes) throws IOException {
        descriptionTemplateService.updateFromTableSource();
        redirectAttributes.addFlashAttribute("infoMessage", "Шаблони звітів груп updated successfully!");
        return "redirect:/admin"; // redirect with flash message
    }

    @PostMapping("/places")
    public String updatePlacesFromTable(RedirectAttributes redirectAttributes) throws IOException {
        placesService.updatePlacesFromTable();
        redirectAttributes.addFlashAttribute("infoMessage", "Regions updated successfully!");
        return "redirect:/admin"; // redirect with flash message
    }

}
