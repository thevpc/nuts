package net.vpc.app.nuts.indexer.services;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.indexer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

@Service
public class RefreshDataService {

    @Autowired
    private DataService dataService;

    private static final Logger logger = LoggerFactory.getLogger(RefreshDataService.class);

    @Autowired
    private NutsIndexSubscriberListManagerPool indexSubscriberListManagerPool;
    @Autowired
    private NutsWorkspacePool workspacePool;
    private NutsIndexSubscriberListManager subscriberManager;

    @PostConstruct
    private void init() {
        subscriberManager = indexSubscriberListManagerPool.openSubscriberListManager("default");
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void refreshData() {
        List<NutsIndexSubscriber> subscribers = subscriberManager.getSubscribers();
        for (NutsIndexSubscriber subscriber : subscribers) {
            logger.info("Refreshing data for subscriber " + subscriber.cacheFolderName() + " started!");

            logger.info("Refreshing components data for subscriber " + subscriber.cacheFolderName() + " started!");
            refreshSubscriberData(subscriber);
            logger.info("Refreshing components data for subscriber " + subscriber.cacheFolderName() + " finished!");

            logger.info("Refreshing data for subscriber " + subscriber.cacheFolderName() + " finished!");
        }
    }

    private void refreshSubscriberData(NutsIndexSubscriber subscriber) {
        Iterator<NutsWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
        if (iterator.hasNext()) {
            NutsWorkspaceLocation workspaceLocation = iterator.next();
            NutsWorkspace ws = workspacePool.openWorkspace(workspaceLocation.getLocation());
            Map<String, NutsId> oldData = this.dataService
                    .getAllData(NutsIndexerUtils.getCacheDir(ws, subscriber.cacheFolderName()))
                    .stream()
                    .collect(Collectors.toMap(map -> map.get("stringId"), map -> NutsIndexerUtils.mapToNutsId(map, ws), (v1, v2) -> v1));
            Iterator<NutsDefinition> definitions = ws.search()
                    .setRepositoryFilter(repository -> repository.getUuid().equals(subscriber.getUuid()))
                    .failFast(false)
                    .installInformation(false)
                    .content(false)
                    .effective(true)
                    .getResultDefinitions().iterator();
            List<Map<String, String>> dataToIndex = new ArrayList<>();
            Map<String, Boolean> visited = new HashMap<>();
            while (definitions.hasNext()) {
                NutsDefinition definition = definitions.next();
                Map<String, String> id = NutsIndexerUtils.nutsIdToMap(definition.getId());
                if (oldData.containsKey(id.get("stringId"))) {
                    visited.put(id.get("stringId"), true);
                    oldData.remove(id.get("stringId"));
                    continue;
                }

                if (visited.getOrDefault(id.get("stringId"), false)) {
                    continue;
                }
                visited.put(id.get("stringId"), true);

                NutsDependency[] directDependencies = definition.getEffectiveDescriptor().getDependencies();
                id.put("dependencies", ws.json().value(Arrays.stream(directDependencies).map(Object::toString).collect(Collectors.toList())).format());
                dataToIndex.add(id);
            }
            this.dataService.indexMultipleData(NutsIndexerUtils.getCacheDir(ws, subscriber.cacheFolderName()), dataToIndex);
            this.dataService.deleteMultipleData(NutsIndexerUtils.getCacheDir(ws, subscriber.cacheFolderName()),
                    oldData.values().stream()
                            .map(NutsIndexerUtils::nutsIdToMap)
                            .collect(Collectors.toList()));
        }
    }



}
