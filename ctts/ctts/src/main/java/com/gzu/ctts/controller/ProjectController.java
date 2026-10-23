package com.gzu.ctts.controller;

import com.gzu.ctts.dto.ProjectDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @GetMapping
    public String projects(@RequestParam Integer page, @RequestParam Integer size) {

        System.out.println("projects page: " + page + ", size: " + size);

        return "Project list";
    }

    @PostMapping
    public String createProject(@RequestBody ProjectDTO projectDTO, @RequestHeader("Authorization") String token) {

        System.out.println("Authorization: " + token);

        System.out.println("createProject: " + projectDTO.toString());
        return "Create project";
    }

    @GetMapping("/{id}")
    public String project(@PathVariable String id) {
        return "Project " + id;
    }


}
