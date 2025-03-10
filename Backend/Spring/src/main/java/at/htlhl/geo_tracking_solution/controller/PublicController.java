package at.htlhl.geo_tracking_solution.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import at.htlhl.geo_tracking_solution.service.GroupService;
import at.htlhl.geo_tracking_solution.service.UserService;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/hello")
    public String sayHello() {
        return "{\"String\": \"Hallo Welt\"}";
    }

}
