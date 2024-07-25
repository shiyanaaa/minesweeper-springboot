package com.minesweeper.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/init")
    public String init() {
        return "Hello World!";
    }
}
