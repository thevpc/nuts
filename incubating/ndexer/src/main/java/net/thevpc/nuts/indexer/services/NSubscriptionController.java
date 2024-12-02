package net.thevpc.nuts.indexer.services;

import javax.annotation.PostConstruct;

import net.thevpc.nuts.*;
import net.thevpc.nuts.indexer.NIndexSubscriberListManager;
import net.thevpc.nuts.indexer.NWorkspacePool;
import net.thevpc.nuts.indexer.NIndexSubscriberListManagerPool;
import net.thevpc.nuts.indexer.NWorkspaceListManagerPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("indexer/subscription")
public class NSubscriptionController {

    @Autowired
    private NWorkspaceListManagerPool listManagerPool;
    @Autowired
    private NIndexSubscriberListManagerPool indexSubscriberListManagerPool;
    @Autowired
    private NWorkspacePool workspacePool;
    private NWorkspaceList workspaceManager;
    private NIndexSubscriberListManager subscriberManager;

    @PostConstruct
    private void init() {
        workspaceManager = listManagerPool.openListManager("default");
        subscriberManager = indexSubscriberListManagerPool.openSubscriberListManager("default");
    }

    @RequestMapping("subscribe")
    public ResponseEntity<Void> subscribe(@RequestParam("workspaceLocation") String workspaceLocation,
            @RequestParam("repositoryUuid") String repositoryUuid) {
        NWorkspace workspace = workspacePool.openWorkspace(workspaceLocation);
        List<NRepository> repositories = NWorkspace.of().getRepositories();
        for (NRepository repository : repositories) {
            if (repository.getUuid().equals(repositoryUuid)) {
                this.subscriberManager.subscribe(repositoryUuid,
                        workspaceManager.getWorkspaceLocation(workspace.getUuid()));

                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @RequestMapping("unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestParam("workspaceLocation") String workspaceLocation,
            @RequestParam("repositoryUuid") String repositoryUuid) {
        NWorkspace workspace = workspacePool.openWorkspace(workspaceLocation);
        List<NRepository> repositories = NWorkspace.of().getRepositories();
        for (NRepository repository : repositories) {
            if (repository.getUuid().equals(repositoryUuid)) {
                this.subscriberManager.unsubscribe(repositoryUuid,
                        workspaceManager.getWorkspaceLocation(workspace.getUuid()));
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @RequestMapping("isSubscribed")
    public ResponseEntity<Boolean> isSubscribed(@RequestParam("workspaceLocation") String workspaceLocation,
            @RequestParam("repositoryUuid") String repositoryUuid) {
        System.out.println(workspaceLocation + " " + repositoryUuid);
        NWorkspace workspace = workspacePool.openWorkspace(workspaceLocation);
        List<NRepository> repositories = NWorkspace.of().getRepositories();
        for (NRepository repository : repositories) {
            if (repository.getUuid().equals(repositoryUuid)) {
                boolean subscribed = this.subscriberManager.isSubscribed(repositoryUuid,
                        workspaceManager.getWorkspaceLocation(workspace.getUuid()));
                return ResponseEntity.ok(subscribed);
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

}
