package net.thevpc.nuts.indexer;

import net.thevpc.nuts.*;
import net.thevpc.common.strings.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class NutsIndexSubscriberListManager {

    private String name;
    private Map<String, NutsIndexSubscriber> subscribers = new LinkedHashMap<>();
    private NutsIndexSubscriberListConfig config;
    private NutsWorkspace defaultWorkspace;

    public NutsIndexSubscriberListManager(NutsWorkspace ws, String name) {
        this.defaultWorkspace = ws;
        if (StringUtils.isBlank(name)) {
            name = "default";
        }
        this.name = name.trim();
        Path file = getConfigFile();
        if (Files.exists(file)) {
            this.config = this.defaultWorkspace.formats().element().setContentType(NutsContentType.JSON).parse(file, NutsIndexSubscriberListConfig.class);
            if (this.config.getSubscribers() != null) {
                for (NutsIndexSubscriber var : this.config.getSubscribers()) {
                    this.subscribers.put(var.getUuid(), var);
                }
            }
        } else {
            this.config = new NutsIndexSubscriberListConfig()
                    .setUuid(UUID.randomUUID().toString())
                    .setName("default-config");
            this.save();
        }
    }

    private Path getConfigFile() {
        return this.defaultWorkspace
                .locations()
                .getStoreLocation(
                        this.defaultWorkspace
                                .id().resolveId(NutsIndexSubscriberListManager.class),
                        NutsStoreLocation.CONFIG).resolve(
                        name + "-nuts-subscriber-list.json");
    }

    public List<NutsIndexSubscriber> getSubscribers() {
        return new ArrayList<>(subscribers.values());
    }

    public NutsIndexSubscriber getSubscriber(String uuid) {
        NutsIndexSubscriber subscriber = subscribers.get(uuid);
        if (subscriber == null) {
            throw new NoSuchElementException("Subscriber with " + uuid + " does not exist!");
        }
        return subscriber.copy();
    }

    public NutsIndexSubscriberListManager setSubscribers(Map<String, NutsIndexSubscriber> subscribers) {
        this.subscribers = subscribers;
        return this;
    }

    public NutsIndexSubscriberListConfig getConfig() {
        return config;
    }

    public NutsIndexSubscriberListManager setConfig(NutsIndexSubscriberListConfig config) {
        this.config = config;
        return this;
    }

    public NutsIndexSubscriber subscribe(String repositoryUuid, NutsWorkspaceLocation workspaceLocation) {
        if (subscribers.containsKey(repositoryUuid)) {
            subscribers.get(repositoryUuid)
                    .getWorkspaceLocations().put(workspaceLocation.getUuid(), workspaceLocation.copy());
        } else {
            subscribers.put(repositoryUuid, new NutsIndexSubscriber()
                    .setUuid(repositoryUuid)
                    .setName(getRepositoryNameFromUuid(repositoryUuid))
                    .setWorkspaceLocations(Collections.singletonMap(workspaceLocation.getUuid(), workspaceLocation.copy())));
        }
        this.save();
        return subscribers.get(repositoryUuid);
    }

    private String getRepositoryNameFromUuid(String repositoryUuid) {
        NutsRepository[] repositories = defaultWorkspace.repos().getRepositories(defaultWorkspace.createSession());
        for (NutsRepository repository : repositories) {
            if (repository.getUuid().equals(repositoryUuid)) {
                return repository.getName();
            }

        }
        throw new NoSuchElementException();
    }

    private void save() {
        this.config.setSubscribers(this.subscribers.isEmpty()
                ? null
                : new ArrayList<>(this.subscribers.values()));
        Path file = getConfigFile();
        this.defaultWorkspace.formats().element().setContentType(NutsContentType.JSON).setValue(this.config).print(file);
    }

    public boolean unsubscribe(String repositoryUuid, NutsWorkspaceLocation workspaceLocation) {
        boolean b = subscribers.get(repositoryUuid)
                .getWorkspaceLocations().remove(workspaceLocation.getUuid()) != null;
        if (subscribers.get(repositoryUuid).getWorkspaceLocations().isEmpty()) {
            b = subscribers.remove(repositoryUuid) != null;
        }
        if (b) {
            save();
        }
        return b;
    }

    public boolean isSubscribed(String repositoryUuid, NutsWorkspaceLocation workspaceLocation) {
        return this.subscribers.containsKey(repositoryUuid)
                && this.subscribers.get(repositoryUuid).getWorkspaceLocations().containsKey(workspaceLocation.getUuid());
    }
}
