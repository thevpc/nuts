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
@RequestMapping("indexer/repositories")
public class NutsRepositoryResource {

    @Autowired
    private DataService dataService;

    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getAll(@RequestParam("workspace") String workspace) {
        NutsWorkspace ws = NutsWorkspacePool.openWorkspace(workspace);
        List<Map<String, String>> rows = this.dataService.
                getAllData(NutsIndexerUtils.getCacheDir(ws, "repositories"));
        List<Map<String, Object>> resData = new ArrayList<>();
        for (Map<String, String> row : rows) {
            Map<String, Object> d = new HashMap<>(row);
            Map[] smirrors = ws.getIOManager().readJson(new StringReader(row.get("mirrors")), Map[].class);
            List<Map<String, String>> mirrors = new ArrayList(Arrays.asList(smirrors));
            d.put("mirrors", mirrors);
            d.put("parents", ws.getIOManager().readJson(new StringReader(row.get("parents")), Map.class));
            resData.add(d);
        }
        return ResponseEntity.ok(resData);
    }

}
