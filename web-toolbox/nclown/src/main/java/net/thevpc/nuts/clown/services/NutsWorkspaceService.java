package net.thevpc.nuts.clown.services;

import net.thevpc.nuts.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.thevpc.nuts.clown.NutsClownUtils;

@RestController
@RequestMapping("ws/workspaces")
public class NutsWorkspaceService {

    @Autowired
    private NutsWorkspaceListManager workspaceManager;
    @Autowired
    private NSession session;

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
        this.workspaceManager.addWorkspace(name,session);
        return getAll();
    }

    @GetMapping(value = "delete", produces = "application/json")
    public ResponseEntity<List<Map<String, String>>> delete(@RequestParam("name") String name) {
        this.workspaceManager.removeWorkspace(name,session);
        return getAll();
    }

    @GetMapping(value = "onOff", produces = "application/json")
    public ResponseEntity<List<Map<String, String>>> onOff(@RequestParam("name") String name,
            @RequestParam("value") Boolean value) {
        this.workspaceManager.getWorkspaceLocation(name).setEnabled(value == null ? false : value);
        this.workspaceManager.save(session);
        return getAll();
    }

    public NSession getWorkspace(String nameOrPath) {
        for (NutsWorkspaceLocation workspace : this.workspaceManager.getWorkspaces()) {
            if (NutsClownUtils.trim(workspace.getName()).equals(nameOrPath)) {
                return NutsWorkspacePool.openWorkspace(workspace.getLocation());
            } else if (NutsClownUtils.trim(workspace.getLocation()).equals(nameOrPath)) {
                return NutsWorkspacePool.openWorkspace(workspace.getLocation());
            }
        }
        return null;
    }
}
