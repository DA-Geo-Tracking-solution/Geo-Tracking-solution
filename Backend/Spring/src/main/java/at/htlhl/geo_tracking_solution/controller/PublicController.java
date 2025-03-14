package at.htlhl.geo_tracking_solution.controller;

import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/hello")
    public String sayHello() {
        return "{\"String\": \"Hallo Welt\"}";
    }

}
