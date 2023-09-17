package controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/first")
public class ControllerFirst {

    @GetMapping("/hello")
    public String hello() {
        return "first/hello";
    }

    @GetMapping("/goodbye")
    public String goodbye() {
        return "first/goodbye";
    }
}
