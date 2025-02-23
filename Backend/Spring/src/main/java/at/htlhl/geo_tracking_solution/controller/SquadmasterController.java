package at.htlhl.geo_tracking_solution.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import at.htlhl.geo_tracking_solution.model.Chat;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/squadmaster")
public class SquadmasterController {

    @GetMapping("/squads")
    @Operation(description = "Returns all Squads of Group")
    public Object getSquad() {
        return new Object();
    }

    @PostMapping("/squad")
    @Operation(description = "Create a squad with squadId and user-emails-array")
    public String createSquad(@RequestBody Chat chat) {
        return "not Successful";
    }

    @PutMapping("/squad/{squadId}")
    @Operation(description = "Edit a squad and its user-emails-array")
    public String editSquad(@PathVariable String squadId, @RequestBody Chat chat) {
        return "not Successful";
    }

    @PostMapping("/Polygon")
    @Operation(description = "Add polygon corners in format { longitude, latitude }")
    public String addPolygon(Object Polygon) {
        return "not Successful";
    }

    @PostMapping("/Circle")
    @Operation(description = "Add Circle in format { { longitude, latitude }, radius }")
    public String addCircle(Object coordinates, double radius) {
        return  "not successful";
    }

}
