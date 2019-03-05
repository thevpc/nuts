package net.vpc.app.nuts.clown.services;

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsFetch;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.io.FileUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("ws/components")
public class NutsComponentService {

    private Logger logger = Logger.getLogger(NutsComponentService.class.getName());

    private static Map<String, String> nutsIdToJson(NutsId id) {
        Map<String, String> res = new HashMap<>();
        res.put("name", id.getName());
        res.put("namespace", id.getNamespace());
        res.put("group", id.getGroup());
        res.put("version", id.getVersion().getValue());
        res.put("scope", id.getScope());
        res.put("arch", id.getArch());
        res.put("os", id.getOs());
        res.put("osdist", id.getOsdist());
        res.put("face", id.getFace());
        return res;
    }

    @SuppressWarnings("unchecked")
    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getAll(@RequestParam("workspace") String workspace) {
        String URL = "http://localhost:7070/indexer/components?workspace=" + workspace;
        RestTemplate template = new RestTemplate();
        return ResponseEntity.ok((List<Map<String, Object>>) template.getForObject(URL, List.class));
    }

    @SuppressWarnings("unchecked")
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

        String URL = String.format("http://localhost:7070/indexer/components/dependencies" +
                "?workspace=%s&name=%s&namespace=%s&group=%s&version=%s&" +
                "face=%s&os=%s&osdist=%s&scope=%s&alternative=%s&arch=%s",
            workspace, name, namespace, group, version, face, os, osdist, scope, alternative, arch);
        RestTemplate template = new RestTemplate();
        return ResponseEntity.ok((List<Map<String, Object>>) template.getForObject(URL, List.class));
    }

    @GetMapping(value = "download", produces = "application/json")
    public ResponseEntity<Resource> downloadComponent(@RequestParam("workspace") String workspace,
                                                      @RequestParam("name") String name,
                                                      @RequestParam("namespace") String namespace,
                                                      @RequestParam("group") String group,
                                                      @RequestParam("version") String version,
                                                      @RequestParam("os") String os,
                                                      @RequestParam("osdist") String osdist,
                                                      @RequestParam("face") String face,
                                                      @RequestParam("scope") String scope,
                                                      @RequestParam("alternative") String alternative) {
        NutsWorkspace ws = Nuts.openWorkspace(workspace);
        NutsId id = ws.createIdBuilder()
            .setName(name)
            .setNamespace(namespace)
            .setGroup(group)
            .setVersion(version)
            .setOs(os)
            .setOsdist(osdist)
            .setFace(face)
            .setScope(scope)
            .setAlternative(alternative)
            .build();
        NutsFetch fetch = ws.fetch(id);
        String filePath = fetch.fetchFile();
        System.out.println(filePath);
        File file = new File(filePath);
        try {
            InputStreamResource resource = new InputStreamResource(
                new FileInputStream(file)
            );
            return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @SuppressWarnings("unchecked")
    @GetMapping(value = "delete", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> deleteComponent(@RequestParam("workspace") String workspace,
                                                                     @RequestParam("name") String name,
                                                                     @RequestParam("namespace") String namespace,
                                                                     @RequestParam("group") String group,
                                                                     @RequestParam("version") String version,
                                                                     @RequestParam("os") String os,
                                                                     @RequestParam("osdist") String osdist,
                                                                     @RequestParam("face") String face,
                                                                     @RequestParam("scope") String scope,
                                                                     @RequestParam("alternative") String alternative) {
        NutsWorkspace ws = Nuts.openWorkspace(workspace);
        NutsId id = ws.createIdBuilder()
            .setName(name)
            .setNamespace(namespace)
            .setGroup(group)
            .setVersion(version)
            .setOs(os)
            .setOsdist(osdist)
            .setFace(face)
            .setScope(scope)
            .setAlternative(alternative)
            .build();
        NutsFetch fetch = ws.fetch(id);
        String filePath = fetch.fetchFile();
        File file = new File(filePath);
        FileUtils.deleteFolderTree(file.getParentFile(), null);
        String URL = String.format("http://localhost:7070/indexer/components/delete" +
                "?workspace=%s&name=%s&namespace=%s&group=%s&version=%s&" +
                "face=%s&os=%s&osdist=%s&scope=%s&alternative=%s&arch=",
            workspace, name, namespace, group, version, face, os, osdist, scope, alternative);
        RestTemplate template = new RestTemplate();
        return ResponseEntity.ok((List<Map<String, Object>>) template.getForObject(URL, List.class));
    }


}
