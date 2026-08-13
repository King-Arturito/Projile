package io.github.kingarturito.projile.project.web;

import io.github.kingarturito.projile.project.ProjectService;
import io.github.kingarturito.projile.project.ProjectView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/{projectId}")
    public ProjectView getProject(@PathVariable UUID projectId, UUID tenantId) {
        return projectService.viewProject(projectId, tenantId);
    }

}