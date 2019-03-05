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

@RestController
@RequestMapping("indexer/components")
public class NutsComponentResource {

    @Autowired
    private DataService dataService;

    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getAll(@RequestParam("workspace") String workspace) {
        NutsWorkspace ws = NutsWorkspacePool.openWorkspace(workspace);
        List<Map<String, Object>> result = this.dataService.
                getAllData(NutsIndexerUtils.getCacheDir(ws, "components"));

        for (Map<String, Object> res : result) {
            String[] array =ws.getIOManager().readJson(new StringReader(res.get("dependencies").toString()),String[].class);
            List<Map<String, String>> dependencies = new ArrayList<>();
            for (String s : array) {
                dependencies.add(NutsIndexerUtils.nutsIdToMap(ws.getParseManager().parseId(s)));
            }
            res.put("dependencies", dependencies);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "dependencies", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getDependencies(@RequestParam("workspace") String workspace,
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
        List<Map<String, Object>> result = this.dataService.
                searchData(NutsIndexerUtils.getCacheDir(ws, "components"), data);
        if (!result.isEmpty()) {
            Map[] array =ws.getIOManager().readJson(new StringReader(result.get(0).get("dependencies").toString()),Map[].class);
            List<Map<String, Object>> dependencies = new ArrayList(Arrays.asList(array));
            result = dependencies;
        }
        return ResponseEntity.ok(result);
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


}
