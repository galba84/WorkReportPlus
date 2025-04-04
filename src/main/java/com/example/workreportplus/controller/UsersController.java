package com.example.workreportplus.controller;

import com.example.workreportplus.dto.UserDto;
import com.example.workreportplus.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UsersController {


    private final UserService userService;

    public UsersController(UserService usersService) {
        this.userService = usersService;
    }

    @GetMapping
    public String users(Model model) {
        List<UserDto> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    @PostMapping("/add")
    public String addUser(UserDto userDto, RedirectAttributes redirectAttributes) {
        userService.addOrUpdateUser(userDto);
        redirectAttributes.addFlashAttribute("infoMessage", "User updated successfully!");
        return "redirect:/users"; // return to admin page after update
    }
}
