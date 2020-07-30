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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("ws/components")
public class NutsComponentService {

    private Logger logger = Logger.getLogger(NutsComponentService.class.getName());

    @Autowired
    private NutsWorkspaceService wss;

    private static Map<String, String> nutsIdToJson(NutsId id) {
        Map<String, String> res = new HashMap<>();
        res.put("name", id.getArtifactId());
        res.put("namespace", id.getNamespace());
        res.put("group", id.getGroupId());
        res.put("version", id.getVersion().getValue());
//        res.put("scope", id.getScope());
        res.put("arch", id.getArch());
        res.put("os", id.getOs());
        res.put("osdist", id.getOsdist());
        res.put("face", id.getFace());
        return res;
    }

    @SuppressWarnings("unchecked")
    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<Map<String, String>>> getAll(@RequestParam("workspace") String workspaceLocation,
            @RequestParam("repository") String repositoryUuid) {
        NutsWorkspace workspace = wss.getWorkspace(workspaceLocation);
        if (workspace == null) {
            return ResponseEntity.ok(new ArrayList<Map<String, String>>());
        }
        List<NutsId> ids = workspace.search()
                .setRepositoryFilter(new NutsRepositoryFilter() {
                    @Override
                    public boolean accept(NutsRepository repository) {
                        return NutsClownUtils.trim(repositoryUuid).isEmpty() ? true : repository.getUuid().equals(repositoryUuid);
                    }
                }).setFailFast(false)
                .setContent(false)
                .setEffective(true)
                .getResultIds().list();
        List<Map<String, String>> result = ids.stream()
                .map(NutsClownUtils::nutsIdToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private NutsId searchId(String workspace,
            String name,
            String namespace,
            String group,
            String version,
            String os,
            String osdist,
            String arch,
            String face,
            String scope,
            //            @RequestParam(NutsConstants.QueryKeys.ALTERNATIVE) String alternative,
            String all) {
        NutsWorkspace _workspace = wss.getWorkspace(workspace);
        if (_workspace == null) {
            return null;
        }
        return _workspace.search()
                .addId(_workspace.id().builder().setArtifactId(name).setGroupId(group).setVersion(version).build())
                .setFailFast(false)
                .setEffective(true)
                .setDependencies(true)
                .getResultIds().first();
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
            //            @RequestParam(NutsConstants.QueryKeys.ALTERNATIVE) String alternative,
            @RequestParam("all") String all) {
        NutsWorkspace _workspace = wss.getWorkspace(workspace);
        if (_workspace != null) {
            NutsId _id = searchId(workspace, name, namespace, group, version, os, osdist, arch, face, scope, all);
            if (_id != null) {
                NutsDefinition d = _workspace.fetch().setDependencies(true).setId(_id).getResultDefinition();
                if (d != null) {
                    List<Map<String, Object>> map = new ArrayList<>();
                    for (NutsDependency dependency : d.getDependencies()) {
                        map.add((Map) NutsClownUtils.nutsIdToMap(_workspace.dependency().builder().setDependency(dependency).getId()));
                    }
                    return ResponseEntity.ok(map);
                }
            }
        }
        return ResponseEntity.ok(new ArrayList<Map<String, Object>>());
//        String URL = String.format("http://localhost:7070/indexer/" + NutsConstants.Folders.ID + "/dependencies"
//                + "?workspace=%s&name=%s&namespace=%s&group=%s&version=%s&"
//                + "face=%s&os=%s&osdist=%s&scope=%s&arch=%s&all=%s"/*&alternative=%s*/,
//                workspace, name, namespace, group, version, face, os, osdist, scope, arch, all/*alternative,*/);
//        RestTemplate template = new RestTemplate();
//        return ResponseEntity.ok((List<Map<String, Object>>) template.getForObject(URL, List.class));
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
            @RequestParam("scope") String scope
    //        ,@RequestParam(NutsConstants.QueryKeys.ALTERNATIVE) String alternative
    ) {
        NutsWorkspace _workspace = wss.getWorkspace(workspace);
        if (_workspace != null) {
            NutsId _id = searchId(workspace, name, namespace, group, version, os, osdist, null, face, scope, null);
            if (_id != null) {
                NutsDefinition d = _workspace.fetch().setContent(true).setId(_id).getResultDefinition();
                if (d != null) {
                    Path filePath = d.getPath();
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
                }
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//
//        NutsWorkspace ws = Nuts.openWorkspace("--workspace", workspace);
//        NutsId id = ws.id().builder()
//                .setArtifactId(name)
//                .setNamespace(namespace)
//                .setGroupId(group)
//                .setVersion(version)
//                .setOs(os)
//                .setOsdist(osdist)
//                .setFace(face)
//                //                .scope(scope)
//                //                .setAlternative(alternative)
//                .build();
//        NutsFetchCommand fetch = ws.fetch().setId(id);
//        Path filePath = fetch.getResultPath();
//        System.out.println(filePath);
//        try {
//            InputStreamResource resource = new InputStreamResource(
//                    Files.newInputStream(filePath)
//            );
//            return ResponseEntity.ok()
//                    .contentLength(Files.size(filePath))
//                    .contentType(MediaType.parseMediaType("application/octet-stream"))
//                    .body(resource);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
            @RequestParam("scope") String scope
    //        ,@RequestParam(NutsConstants.QueryKeys.ALTERNATIVE) String alternative
    ) {
        NutsWorkspace _workspace = wss.getWorkspace(workspace);
        if (_workspace != null) {
            NutsId _id = searchId(workspace, name, namespace, group, version, os, osdist, null, face, scope, null);
            if (_id != null) {
                _workspace.undeploy().addId(_id).run();
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        
//        NutsWorkspace ws = Nuts.openWorkspace("--workspace", workspace);
//        NutsId id = ws.id().builder()
//                .setArtifactId(name)
//                .setNamespace(namespace)
//                .setGroupId(group)
//                .setVersion(version)
//                .setOs(os)
//                .setOsdist(osdist)
//                .setFace(face)
//                //                .scope(scope)
//                //                .setAlternative(alternative)
//                .build();
//        ws.undeploy().addId(id).run();
//        String URL = String.format("http://localhost:7070/indexer/" + NutsConstants.Folders.ID + "/delete"
//                + "?workspace=%s&name=%s&namespace=%s&group=%s&version=%s&"
//                + "face=%s&os=%s&osdist=%s&scope=%s&arch="/*&alternative=%s*/,
//                workspace, name, namespace, group, version, face, os, osdist, scope/*, alternative*/);
//        RestTemplate template = new RestTemplate();
//        return ResponseEntity.ok((List<Map<String, Object>>) template.getForObject(URL, List.class));
    }

}
