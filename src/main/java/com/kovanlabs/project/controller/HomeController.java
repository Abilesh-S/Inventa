package com.kovanlabs.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.logging.Logger;

@Controller
public class HomeController {
    Logger LOG = Logger.getLogger(HomeController.class.getName());
    @GetMapping("/")
    public String homePage() {
        LOG.info("Runned Successfully");
        return "home";
    }
}
