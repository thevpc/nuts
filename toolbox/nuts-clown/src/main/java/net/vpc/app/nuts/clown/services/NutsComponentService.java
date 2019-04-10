package net.vpc.app.nuts.clown.services;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.clown.NutsClownUtils;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.vpc.common.io.IOUtils;

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
    public ResponseEntity<List<Map<String, String>>> getAll(@RequestParam("workspaceLocation") String workspaceLocation,
            @RequestParam("repositoryUuid") String repositoryUuid) {
        NutsWorkspace workspace = Nuts.openWorkspace("--workspace",workspaceLocation);
        List<NutsId> ids = workspace.find()
                .setRepositoryFilter(new NutsRepositoryFilter() {
                    @Override
                    public boolean accept(NutsRepository repository) {
                        return repository.getUuid().equals(repositoryUuid);
                    }
                })
                .setLenient(true)
                .setIncludeInstallInformation(false)
                .setIncludeContent(false)
                .effective(true)
                .getResultIds().list();
        List<Map<String, String>> result = ids.stream()
                .map(NutsClownUtils::nutsIdToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
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
            @RequestParam("alternative") String alternative,
            @RequestParam("all") String all) {

        String URL = String.format("http://localhost:7070/indexer/"+NutsConstants.Folders.LIB+"/dependencies"
                + "?workspace=%s&name=%s&namespace=%s&group=%s&version=%s&"
                + "face=%s&os=%s&osdist=%s&scope=%s&alternative=%s&arch=%s&all=%s",
                workspace, name, namespace, group, version, face, os, osdist, scope, alternative, arch, all);
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
        NutsWorkspace ws = Nuts.openWorkspace("--workspace",workspace);
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
        NutsFetchCommand fetch = ws.fetch().id(id);
        Path filePath = fetch.getResultPath();
        System.out.println(filePath);
        try {
            InputStreamResource resource = new InputStreamResource(
                    Files.newInputStream(filePath)
            );
            return ResponseEntity.ok()
                    .contentLength(Files.size(filePath))
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(resource);
        } catch (IOException e) {
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
        NutsWorkspace ws = Nuts.openWorkspace("--workspace",workspace);
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
        NutsFetchCommand fetch = ws.fetch().id(id);
        Path filePath = fetch.getResultPath();
        try {
            IOUtils.delete(filePath.getParent());
        } catch (IOException ex) {
            Logger.getLogger(NutsComponentService.class.getName()).log(Level.SEVERE, null, ex);
        }
        String URL = String.format("http://localhost:7070/indexer/"+NutsConstants.Folders.LIB+"/delete"
                + "?workspace=%s&name=%s&namespace=%s&group=%s&version=%s&"
                + "face=%s&os=%s&osdist=%s&scope=%s&alternative=%s&arch=",
                workspace, name, namespace, group, version, face, os, osdist, scope, alternative);
        RestTemplate template = new RestTemplate();
        return ResponseEntity.ok((List<Map<String, Object>>) template.getForObject(URL, List.class));
    }

}
