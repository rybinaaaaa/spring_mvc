package rybina.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("first")
public class ControllerFirst {

    // First Method
    @GetMapping("/hello")
    public String hello(HttpServletRequest request) {
        String name = request.getParameter("name");
        System.out.println("hello " + name);
        return "first/hello";
    }

    // Second method
    @GetMapping("/goodbye")
    public String goodbye(@RequestParam(value = "name", required = false) String name, Model model) {
        model.addAttribute("message","Bye " + name + "!");
        return "first/goodbye";
    }
}
