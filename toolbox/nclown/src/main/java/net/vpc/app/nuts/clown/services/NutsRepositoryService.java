package net.thevpc.nuts.clown.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsWorkspace;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsRepositoryDefinition;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("ws/"+NutsConstants.Folders.REPOSITORIES)
public class NutsRepositoryService {

    private Logger logger = Logger.getLogger(NutsRepositoryService.class.getName());
    @Autowired
    private NutsWorkspaceService wss;

    @SuppressWarnings("unchecked")
    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getAllRepositories(@RequestParam("workspace") String workspace) {
        String URL = "http://localhost:7070/indexer/"+NutsConstants.Folders.REPOSITORIES+"?workspace=" + workspace;
        RestTemplate template = new RestTemplate();
        return ResponseEntity.ok((List<Map<String, Object>>) template.getForObject(URL, List.class));
    }

    @GetMapping(value = "delete", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> deleteRepository(@RequestParam("name") String name,
            @RequestParam("workspace") String workspace) {
        NutsWorkspace ws = wss.getWorkspace(workspace);
        ws.config().removeRepository(name,null);
        ws.config().save(null);
        logger.info(String.format("Repository with name %s was deleted", name));
        return getAllRepositories(workspace);
    }

    @SuppressWarnings("unchecked")
    @GetMapping(value = "add", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> addRepository(@RequestParam("workspace") String workspace,
            @RequestParam("data") String data) {
        NutsWorkspace ws = wss.getWorkspace(workspace);
        try {
            HashMap<String, String> dataMap = new ObjectMapper().readValue(data, HashMap.class);
            NutsRepositoryDefinition location = new NutsRepositoryDefinition()
                    .setName(dataMap.get("name"))
                    .setLocation(dataMap.get("location"))
                    .setType(dataMap.get("type"));
            ws.config().addRepository(location);
            ws.config().save(null);
            logger.info(String.format("Repository with name %s was created", location.getName()));
        } catch (NutsIllegalArgumentException | IOException ex) {
            logger.info(ex.getMessage());
        }
        return getAllRepositories(workspace);
    }

}
