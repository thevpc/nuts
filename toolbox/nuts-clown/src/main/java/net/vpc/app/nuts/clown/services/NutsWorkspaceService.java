package net.vpc.app.nuts.clown.services;

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspaceListManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("ws/workspaces")
public class NutsWorkspaceService {

    private NutsWorkspaceListManager workspaceManager= Nuts.openWorkspace().config().createWorkspaceListManager("clown");

    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<Map<String, String>>> getAll() {
        List<Map<String, String>> result = this.workspaceManager
            .getWorkspaces()
            .stream()
            .map(workspace -> {
                Map<String, String> res = new LinkedHashMap<>();
                res.put("name", workspace.getName());
                res.put("location", workspace.getLocation());
                res.put("enabled", String.valueOf(workspace.isEnabled()));
                return res;
            }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "add", produces = "application/json")
    public ResponseEntity<List<Map<String, String>>> add(@RequestParam("name") String name) {
        this.workspaceManager.addWorkspace(name);
        return getAll();
    }

    @GetMapping(value = "delete", produces = "application/json")
    public ResponseEntity<List<Map<String, String>>> delete(@RequestParam("name") String name) {
        this.workspaceManager.removeWorkspace(name);
        return getAll();
    }

    @GetMapping(value = "onOff", produces = "application/json")
    public ResponseEntity<List<Map<String, String>>> onOff(@RequestParam("name") String name,
                                                           @RequestParam("value") Boolean value) {
        this.workspaceManager.onOffWorkspace(name, value);
        return getAll();
    }
}
