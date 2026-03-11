package com.gotokart.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SayHi {

    @GetMapping("/")
    public String SayHi() {
        return "Hello, Ankit This Side";
    }
}