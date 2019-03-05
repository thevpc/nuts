package net.vpc.app.nuts.indexer.services;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.indexer.NutsIndexerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("indexer/components")
public class NutsComponentResource {

    @Autowired
    private DataService dataService;

    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getAll(@RequestParam("workspace") String workspace) {
        NutsWorkspace ws = NutsWorkspacePool.openWorkspace(workspace);
        List<Map<String, String>> rows = this.dataService.
                getAllData(NutsIndexerUtils.getCacheDir(ws, "components"));
        List<Map<String, Object>> resData = cleanNutsIdMap(ws, rows);
        return ResponseEntity.ok(resData);
    }

    @GetMapping(value = "dependencies", produces = "application/json")
    public ResponseEntity<List<Map<String, String>>> getDependencies(@RequestParam("workspace") String workspace,
                                                                     @RequestParam("name") String name,
                                                                     @RequestParam("namespace") String namespace,
                                                                     @RequestParam("group") String group,
                                                                     @RequestParam("version") String version,
                                                                     @RequestParam("os") String os,
                                                                     @RequestParam("osdist") String osdist,
                                                                     @RequestParam("arch") String arch,
                                                                     @RequestParam("face") String face,
                                                                     @RequestParam("scope") String scope,
                                                                     @RequestParam("alternative") String alternative,
                                                                     @RequestParam("all") Boolean all) {
        NutsWorkspace ws = NutsWorkspacePool.openWorkspace(workspace);
        NutsId id = ws.createIdBuilder()
                .setName(name)
                .setNamespace(namespace)
                .setGroup(group)
                .setVersion(version)
                .setArch(arch)
                .setOs(os)
                .setOsdist(osdist)
                .setFace(face)
                .setScope(scope)
                .setAlternative(alternative)
                .build();
        List<Map<String, String>> result;
        if (all) {
            result = this.dataService.getAllDependencies(ws, NutsIndexerUtils.getCacheDir(ws, "components"), id);
        } else {
            result = this.dataService.getDependencies(ws, NutsIndexerUtils.getCacheDir(ws, "components"), id);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "allVersions", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getAllVersions(@RequestParam("workspace") String workspace,
                                                                    @RequestParam("name") String name,
                                                                    @RequestParam("namespace") String namespace,
                                                                    @RequestParam("group") String group,
                                                                    @RequestParam("os") String os,
                                                                    @RequestParam("osdist") String osdist,
                                                                    @RequestParam("arch") String arch,
                                                                    @RequestParam("face") String face,
                                                                    @RequestParam("scope") String scope,
                                                                    @RequestParam("alternative") String alternative) {
        NutsWorkspace ws = NutsWorkspacePool.openWorkspace(workspace);
        NutsId id = ws.createIdBuilder()
                .setName(name)
                .setNamespace(namespace)
                .setGroup(group)
                .setArch(arch)
                .setOs(os)
                .setOsdist(osdist)
                .setFace(face)
                .setScope(scope)
                .setAlternative(alternative)
                .build();
        List<Map<String, String>> rows = this.dataService.getAllVersions(ws, NutsIndexerUtils.getCacheDir(ws, "components"), id);
        List<Map<String, Object>> resData = cleanNutsIdMap(ws, rows);
        return ResponseEntity.ok(resData);
    }

    @GetMapping(value = "delete", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> deleteComponent(@RequestParam("workspace") String workspace,
                                                                     @RequestParam("name") String name,
                                                                     @RequestParam("namespace") String namespace,
                                                                     @RequestParam("group") String group,
                                                                     @RequestParam("version") String version,
                                                                     @RequestParam("os") String os,
                                                                     @RequestParam("osdist") String osdist,
                                                                     @RequestParam("arch") String arch,
                                                                     @RequestParam("face") String face,
                                                                     @RequestParam("scope") String scope,
                                                                     @RequestParam("alternative") String alternative) {
        NutsWorkspace ws = NutsWorkspacePool.openWorkspace(workspace);
        Map<String, String> data = NutsIndexerUtils.nutsIdToMap(
                ws.createIdBuilder()
                        .setName(name)
                        .setNamespace(namespace)
                        .setGroup(group)
                        .setVersion(version)
                        .setArch(arch)
                        .setOs(os)
                        .setOsdist(osdist)
                        .setFace(face)
                        .setScope(scope)
                        .setAlternative(alternative)
                        .build());
        this.dataService.deleteData(NutsIndexerUtils.getCacheDir(ws, "components"), data);
        return getAll(workspace);
    }

    private List<Map<String, Object>> cleanNutsIdMap(NutsWorkspace ws, List<Map<String, String>> rows) {
        List<Map<String, Object>> resData = new ArrayList<>();
        for (Map<String, String> row : rows) {
            Map<String, Object> d = new HashMap<>(row);
            String[] array = ws.getIOManager().readJson(new StringReader(row.get("dependencies")), String[].class);
            List<Map<String, String>> dependencies = new ArrayList<>();
            for (String s : array) {
                dependencies.add(NutsIndexerUtils.nutsIdToMap(ws.getParseManager().parseId(s)));
            }
            d.put("dependencies", dependencies);
            if (d.containsKey("allDependencies")) {
                array = ws.getIOManager().readJson(new StringReader(row.get("allDependencies")), String[].class);
                List<Map<String, String>> allDependencies = new ArrayList<>();
                for (String s : array) {
                    allDependencies.add(NutsIndexerUtils.nutsIdToMap(ws.getParseManager().parseId(s)));
                }
                d.put("allDependencies", allDependencies);
            }
            resData.add(d);
        }
        return resData;
    }
}
