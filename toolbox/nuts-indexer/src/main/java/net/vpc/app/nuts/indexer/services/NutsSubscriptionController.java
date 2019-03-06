package net.vpc.app.nuts.indexer.services;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.indexer.NutsIndexSubscriberListManager;
import net.vpc.app.nuts.indexer.NutsIndexSubscriberListManagerPool;
import net.vpc.app.nuts.indexer.NutsWorkspaceListManagerPool;
import net.vpc.app.nuts.indexer.NutsWorkspacePool;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("indexer/subscription")
public class NutsSubscriptionController {

    private NutsWorkspaceListManager workspaceManager = NutsWorkspaceListManagerPool.openListManager("default");
    private NutsIndexSubscriberListManager subscriberManager = NutsIndexSubscriberListManagerPool.openSubscriberListManager("default");

    @RequestMapping("subscribe")
    public ResponseEntity<Void> subscribe(@RequestParam("workspaceLocation") String workspaceLocation,
                                          @RequestParam("repositoryUuid") String repositoryUuid) {
        NutsWorkspace workspace = Nuts.openWorkspace(workspaceLocation);
        NutsRepository[] repositories = workspace.getRepositoryManager().getRepositories();
        for (NutsRepository repository : repositories) {
            if (repository.getUuid().equals(repositoryUuid)) {
                this.subscriberManager.subscribe(repositoryUuid
                        , workspaceManager.getWorkspaceLocation(workspace.getUuid()));

                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @RequestMapping("unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestParam("workspaceLocation") String workspaceLocation,
                                            @RequestParam("repositoryUuid") String repositoryUuid) {
        NutsWorkspace workspace = Nuts.openWorkspace(workspaceLocation);
        NutsRepository[] repositories = workspace.getRepositoryManager().getRepositories();
        for (NutsRepository repository : repositories) {
            if (repository.getUuid().equals(repositoryUuid)) {
                this.subscriberManager.unsubscribe(repositoryUuid
                        , workspaceManager.getWorkspaceLocation(workspace.getUuid()));

                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @RequestMapping("isSubscribed")
    public ResponseEntity<Boolean> isSubscribed(@RequestParam("workspaceLocation") String workspaceLocation,
                                                @RequestParam("repositoryUuid") String repositoryUuid) {
        System.out.println(workspaceLocation + " " + repositoryUuid);
        NutsWorkspace workspace = Nuts.openWorkspace(workspaceLocation);
        NutsRepository[] repositories = workspace.getRepositoryManager().getRepositories();
        for (NutsRepository repository : repositories) {
            if (repository.getUuid().equals(repositoryUuid)) {
                boolean subscribed = this.subscriberManager.isSubscribed(repositoryUuid
                        , workspaceManager.getWorkspaceLocation(workspace.getUuid()));

                return ResponseEntity.ok(subscribed);
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

}
