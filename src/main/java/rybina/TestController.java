package rybina;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {
    @GetMapping("/hi")
    @ResponseBody
    public String sayHi() {
        return "hi";
    }
}
