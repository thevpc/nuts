package net.thevpc.nuts.indexer.services;

import javax.annotation.PostConstruct;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.indexer.NutsIndexSubscriberListManager;
import net.thevpc.nuts.indexer.NutsWorkspacePool;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsWorkspaceListManager;
import net.thevpc.nuts.indexer.NutsIndexSubscriberListManagerPool;
import net.thevpc.nuts.indexer.NutsWorkspaceListManagerPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("indexer/subscription")
public class NutsSubscriptionController {

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

    @RequestMapping("subscribe")
    public ResponseEntity<Void> subscribe(@RequestParam("workspaceLocation") String workspaceLocation,
            @RequestParam("repositoryUuid") String repositoryUuid) {
        NutsSession session = workspacePool.openWorkspace(workspaceLocation);
        NutsRepository[] repositories = session.repos().getRepositories();
        for (NutsRepository repository : repositories) {
            if (repository.getUuid().equals(repositoryUuid)) {
                this.subscriberManager.subscribe(repositoryUuid,
                        workspaceManager.getWorkspaceLocation(session.getWorkspace().getUuid()), this.subscriberManager.getDefaultWorkspace().createSession());

                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @RequestMapping("unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestParam("workspaceLocation") String workspaceLocation,
            @RequestParam("repositoryUuid") String repositoryUuid) {
        NutsSession session = workspacePool.openWorkspace(workspaceLocation);
        NutsRepository[] repositories = session.repos().getRepositories();
        for (NutsRepository repository : repositories) {
            if (repository.getUuid().equals(repositoryUuid)) {
                this.subscriberManager.unsubscribe(repositoryUuid,
                        workspaceManager.getWorkspaceLocation(session.getWorkspace().getUuid()), this.subscriberManager.getDefaultWorkspace().createSession());
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @RequestMapping("isSubscribed")
    public ResponseEntity<Boolean> isSubscribed(@RequestParam("workspaceLocation") String workspaceLocation,
            @RequestParam("repositoryUuid") String repositoryUuid) {
        System.out.println(workspaceLocation + " " + repositoryUuid);
        NutsSession session = workspacePool.openWorkspace(workspaceLocation);
        NutsRepository[] repositories = session.repos().getRepositories();
        for (NutsRepository repository : repositories) {
            if (repository.getUuid().equals(repositoryUuid)) {
                boolean subscribed = this.subscriberManager.isSubscribed(repositoryUuid,
                        workspaceManager.getWorkspaceLocation(session.getWorkspace().getUuid()));
                return ResponseEntity.ok(subscribed);
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

}
