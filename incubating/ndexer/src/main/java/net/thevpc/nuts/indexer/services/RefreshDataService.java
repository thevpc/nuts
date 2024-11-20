package net.thevpc.nuts.indexer.services;

import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.indexer.*;
import net.thevpc.nuts.*;
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
    private NIndexSubscriberListManagerPool indexSubscriberListManagerPool;
    @Autowired
    private NWorkspacePool workspacePool;
    private NIndexSubscriberListManager subscriberManager;

    @PostConstruct
    private void init() {
        subscriberManager = indexSubscriberListManagerPool.openSubscriberListManager("default");
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void refreshData() {
        List<NIndexSubscriber> subscribers = subscriberManager.getSubscribers();
        for (NIndexSubscriber subscriber : subscribers) {
            logger.info("Refreshing data for subscriber " + subscriber.cacheFolderName() + " started!");

            logger.info("Refreshing components data for subscriber " + subscriber.cacheFolderName() + " started!");
            refreshSubscriberData(subscriber);
            logger.info("Refreshing components data for subscriber " + subscriber.cacheFolderName() + " finished!");

            logger.info("Refreshing data for subscriber " + subscriber.cacheFolderName() + " finished!");
        }
    }

    private void refreshSubscriberData(NIndexSubscriber subscriber) {
        Iterator<NWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
        if (iterator.hasNext()) {
            NWorkspaceLocation workspaceLocation = iterator.next();
            NSession session = workspacePool.openWorkspace(workspaceLocation.getLocation());
            Map<String, NId> oldData = this.dataService
                    .getAllData(NIndexerUtils.getCacheDir(subscriber.cacheFolderName()))
                    .stream()
                    .collect(Collectors.toMap(map -> map.get("stringId"), map -> NIndexerUtils.mapToNutsId(map), (v1, v2) -> v1));
            Iterator<NDefinition> definitions = NSearchCmd.of()
                    .setRepositoryFilter(NRepositories.of().filter().byUuid(subscriber.getUuid()))
                    .setFailFast(false)
                    .setContent(false)
                    .setEffective(true)
                    .getResultDefinitions().iterator();
            List<Map<String, String>> dataToIndex = new ArrayList<>();
            Map<String, Boolean> visited = new HashMap<>();
            while (definitions.hasNext()) {
                NDefinition definition = definitions.next();
                Map<String, String> id = NIndexerUtils.nutsIdToMap(definition.getId());
                if (oldData.containsKey(id.get("stringId"))) {
                    visited.put(id.get("stringId"), true);
                    oldData.remove(id.get("stringId"));
                    continue;
                }

                if (visited.getOrDefault(id.get("stringId"), false)) {
                    continue;
                }
                visited.put(id.get("stringId"), true);

                List<NDependency> directDependencies = definition.getEffectiveDescriptor().get().getDependencies();
                id.put("dependencies",
                        NElements.of().json().setValue(directDependencies.stream().map(Object::toString).collect(Collectors.toList()))
                                .setNtf(false).format().filteredText()
                );
                dataToIndex.add(id);
            }
            this.dataService.indexMultipleData(NIndexerUtils.getCacheDir(subscriber.cacheFolderName()), dataToIndex);
            this.dataService.deleteMultipleData(NIndexerUtils.getCacheDir(subscriber.cacheFolderName()),
                    oldData.values().stream()
                            .map(NIndexerUtils::nutsIdToMap)
                            .collect(Collectors.toList()));
        }
    }



}
