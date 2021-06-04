package net.thevpc.nuts.indexer.services;

import net.thevpc.nuts.indexer.*;
import net.thevpc.nuts.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

@RestController
@RequestMapping("indexer/"+NutsConstants.Folders.ID)
public class NutsComponentController {

    @Autowired
    private DataService dataService;
    private static final Logger LOG = LoggerFactory.getLogger(NutsComponentController.class);

        @Autowired
    private NutsWorkspaceListManagerPool listManagerPool;
    @Autowired
    private NutsIndexSubscriberListManagerPool indexSubscriberListManagerPool;
    @Autowired
    private NutsWorkspacePool workspacePool;
    private NutsWorkspaceListManager workspaceManager;
    private NutsIndexSubscriberListManager subscriberManager;

    @PostConstruct
    private void init() {
        workspaceManager = listManagerPool.openListManager("default");
        subscriberManager = indexSubscriberListManagerPool.openSubscriberListManager("default");
    }


    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getAll(@RequestParam("repositoryUuid") String repositoryUuid) {
        NutsIndexSubscriber subscriber = subscriberManager.getSubscriber(repositoryUuid);
        if (subscriber != null) {
            LOG.info("Getting all components data for subscriber " + subscriber.cacheFolderName());
            Iterator<NutsWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
            if (iterator.hasNext()) {
                NutsWorkspaceLocation workspaceLocation = iterator.next();
                NutsWorkspace ws = Nuts.openWorkspace("--workspace",workspaceLocation.getLocation());
                List<Map<String, String>> rows = this.dataService.
                        getAllData(NutsIndexerUtils.getCacheDir(ws.createSession(), subscriber.cacheFolderName()));
                List<Map<String, Object>> resData = cleanNutsIdMap(ws, rows);
                return ResponseEntity.ok(resData);
            }
        }
        LOG.error("Error in getting all components data for subscriber " + repositoryUuid);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping(value = "dependencies", produces = "application/json")
    public ResponseEntity<List<Map<String, String>>> getDependencies(@RequestParam("repositoryUuid") String repositoryUuid,
                                                                     @RequestParam("name") String name,
                                                                     @RequestParam("namespace") String namespace,
                                                                     @RequestParam("group") String group,
                                                                     @RequestParam("version") String version,
                                                                     @RequestParam("os") String os,
                                                                     @RequestParam("osdist") String osdist,
                                                                     @RequestParam("arch") String arch,
                                                                     @RequestParam("face") String face,
//                                                                     @RequestParam(NutsConstants.IdProperties.ALTERNATIVE) String alternative,
                                                                     @RequestParam("all") Boolean all) {
        NutsIndexSubscriber subscriber = subscriberManager.getSubscriber(repositoryUuid);
        if (subscriber != null) {
            LOG.info("Getting dependencies of component " + name + " data for subscriber " + subscriber.cacheFolderName());
            Iterator<NutsWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
            if (iterator.hasNext()) {
                NutsWorkspaceLocation workspaceLocation = iterator.next();
                NutsWorkspace ws = Nuts.openWorkspace("--workspace",workspaceLocation.getLocation());
                NutsSession session = ws.createSession();
                NutsId id = ws.id().builder()
                        .setArtifactId(name)
                        .setNamespace(namespace)
                        .setGroupId(group)
                        .setVersion(version)
                        .setArch(arch)
                        .setOs(os)
                        .setOsdist(osdist)
                        .setFace(face)
//                        .setAlternative(alternative)
                        .build();
                List<Map<String, String>> result;
                if (all) {
                    result = this.dataService.getAllDependencies(ws, NutsIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()), id);
                } else {
                    result = this.dataService.getDependencies(ws, NutsIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()), id);
                }
                return ResponseEntity.ok(result);
            }
        }
        LOG.error("Error in getting dependencies of component " + name + " data for subscriber " + repositoryUuid);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping(value = "allVersions", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getAllVersions(@RequestParam("repositoryUuid") String repositoryUuid,
                                                                    @RequestParam("name") String name,
                                                                    @RequestParam("namespace") String namespace,
                                                                    @RequestParam("group") String group,
                                                                    @RequestParam("os") String os,
                                                                    @RequestParam("osdist") String osdist,
                                                                    @RequestParam("arch") String arch,
                                                                    @RequestParam("face") String face
//            ,@RequestParam(NutsConstants.IdProperties.ALTERNATIVE) String alternative
    ) {
        NutsIndexSubscriber subscriber = subscriberManager.getSubscriber(repositoryUuid);
        if (subscriber != null) {
            LOG.info("Getting all versions of component " + name + " data for subscriber " + subscriber.cacheFolderName());
            Iterator<NutsWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
            if (iterator.hasNext()) {
                NutsWorkspaceLocation workspaceLocation = iterator.next();
                NutsWorkspace ws = Nuts.openWorkspace("--workspace",workspaceLocation.getLocation());
                NutsId id = ws.id().builder()
                        .setArtifactId(name)
                        .setNamespace(namespace)
                        .setGroupId(group)
                        .setArch(arch)
                        .setOs(os)
                        .setOsdist(osdist)
                        .setFace(face)
//                        .setAlternative(alternative)
                        .build();
                List<Map<String, String>> rows = this.dataService.getAllVersions(ws, NutsIndexerUtils.getCacheDir(ws.createSession(), subscriber.cacheFolderName()), id);
                List<Map<String, Object>> resData = cleanNutsIdMap(ws, rows);
                return ResponseEntity.ok(resData);
            }
        }
        LOG.error("Error in getting all versions of component " + name + " data for subscriber " + repositoryUuid);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping(value = "delete", produces = "application/json")
    public ResponseEntity<Boolean> deleteComponent(@RequestParam("repositoryUuid") String repositoryUuid,
                                                   @RequestParam("name") String name,
                                                   @RequestParam("namespace") String namespace,
                                                   @RequestParam("group") String group,
                                                   @RequestParam("version") String version,
                                                   @RequestParam("os") String os,
                                                   @RequestParam("osdist") String osdist,
                                                   @RequestParam("arch") String arch,
                                                   @RequestParam("face") String face
//            ,@RequestParam(NutsConstants.IdProperties.ALTERNATIVE) String alternative
    ) {
        NutsIndexSubscriber subscriber = subscriberManager.getSubscriber(repositoryUuid);
        if (subscriber != null) {
            LOG.info("Deleting the component " + name + " data for subscriber " + subscriber.cacheFolderName());
            Iterator<NutsWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
            if (iterator.hasNext()) {
                NutsWorkspaceLocation workspaceLocation = iterator.next();
                NutsWorkspace ws = Nuts.openWorkspace("--workspace",workspaceLocation.getLocation());
                Map<String, String> data = NutsIndexerUtils.nutsIdToMap(
                        ws.id().builder()
                                .setArtifactId(name)
                                .setNamespace(namespace)
                                .setGroupId(group)
                                .setVersion(version)
                                .setArch(arch)
                                .setOs(os)
                                .setOsdist(osdist)
                                .setFace(face)
//                                .setAlternative(alternative)
                                .build());
                this.dataService.deleteData(NutsIndexerUtils.getCacheDir(ws.createSession(), subscriber.cacheFolderName()), data);
                return ResponseEntity.ok(true);
            }
        }
        LOG.error("Error in deleting the component " + name + " data for subscriber " + repositoryUuid);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping(value = "addData", produces = "application/json")
    public ResponseEntity<Boolean> addDocument(@RequestParam("repositoryUuid") String repositoryUuid,
                                               @RequestParam("name") String name,
                                               @RequestParam("namespace") String namespace,
                                               @RequestParam("group") String group,
                                               @RequestParam("version") String version,
                                               @RequestParam("os") String os,
                                               @RequestParam("osdist") String osdist,
                                               @RequestParam("arch") String arch,
                                               @RequestParam("face") String face
//            ,@RequestParam(NutsConstants.IdProperties.ALTERNATIVE) String alternative
    ) {
        NutsIndexSubscriber subscriber = subscriberManager.getSubscriber(repositoryUuid);
        if (subscriber != null) {
            LOG.info("Getting all versions of component " + name + " data for subscriber " + subscriber.cacheFolderName());
            Iterator<NutsWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
            if (iterator.hasNext()) {
                NutsWorkspaceLocation workspaceLocation = iterator.next();
                NutsWorkspace ws = Nuts.openWorkspace("--workspace",workspaceLocation.getLocation());
                NutsId id = ws.id().builder()
                        .setArtifactId(name)
                        .setNamespace(namespace)
                        .setGroupId(group)
                        .setVersion(version)
                        .setArch(arch)
                        .setOs(os)
                        .setOsdist(osdist)
                        .setFace(face)
//                        .setAlternative(alternative)
                        .build();
                Map<String, String> data = NutsIndexerUtils.nutsIdToMap(id);
                NutsSession session = ws.createSession();
                List<Map<String, String>> list = this.dataService.searchData(NutsIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()), data, null);
                if (list.isEmpty()) {
                    Iterator<NutsDefinition> it = ws.search()
                            .setRepositoryFilter(
                                    ws.repos().filter().byUuid(subscriber.getUuid())
                            )
                            .addId(id)
                            .setFailFast(false)
                            .setContent(false)
                            .setEffective(true)
                            .getResultDefinitions().iterator();
                    if (it.hasNext()) {
                        NutsDefinition definition = it.next();
                        NutsDependency[] directDependencies = definition.getEffectiveDescriptor().getDependencies();
                        data.put("dependencies", ws.elem().setContentType(NutsContentType.JSON)
                                .setValue(Arrays.stream(directDependencies).map(Object::toString)
                                        .collect(Collectors.toList()))
                                        .setNtf(false)
                                .format()
                                .toString()
                        );

                        this.dataService.indexData(NutsIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()), data);
                    } else {
                        ResponseEntity.ok(false);
                    }
                }
                return ResponseEntity.ok(true);
            }
        }
        LOG.error("Error in deleting the component " + name + " data for subscriber " + repositoryUuid);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private List<Map<String, Object>> cleanNutsIdMap(NutsWorkspace ws, List<Map<String, String>> rows) {
        List<Map<String, Object>> resData = new ArrayList<>();
        for (Map<String, String> row : rows) {
            Map<String, Object> d = new HashMap<>(row);
            if (d.containsKey("dependencies")) {
                String[] array = ws.elem().setContentType(NutsContentType.JSON).parse(new StringReader(row.get("dependencies")), String[].class);
                List<Map<String, String>> dependencies = new ArrayList<>();
                for (String s : array) {
                    dependencies.add(NutsIndexerUtils.nutsIdToMap(ws.id().parser().parse(s)));
                }
                d.put("dependencies", dependencies);
            }
            if (d.containsKey("allDependencies")) {
                String[] array = ws.elem().setContentType(NutsContentType.JSON).parse(new StringReader(row.get("allDependencies")), String[].class);
                List<Map<String, String>> allDependencies = new ArrayList<>();
                for (String s : array) {
                    allDependencies.add(NutsIndexerUtils.nutsIdToMap(ws.id().parser().parse(s)));
                }
                d.put("allDependencies", allDependencies);
            }
            resData.add(d);
        }
        return resData;
    }
}
