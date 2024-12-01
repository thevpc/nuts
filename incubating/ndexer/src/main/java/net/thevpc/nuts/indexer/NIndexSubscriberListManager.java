package net.thevpc.nuts.indexer;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;

import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;

import java.util.*;

public class NIndexSubscriberListManager {

    private String name;
    private Map<String, NIndexSubscriber> subscribers = new LinkedHashMap<>();
    private NIndexSubscriberListConfig config;

    public NIndexSubscriberListManager(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "default";
        }
        this.name = name.trim();
        NPath file = getConfigFile();
        if (file.exists()) {
            this.config = NElements.of().json().parse(file, NIndexSubscriberListConfig.class);
            if (this.config.getSubscribers() != null) {
                for (NIndexSubscriber var : this.config.getSubscribers()) {
                    this.subscribers.put(var.getUuid(), var);
                }
            }
        } else {
            this.config = new NIndexSubscriberListConfig()
                    .setUuid(UUID.randomUUID().toString())
                    .setName("default-config");
            this.save();
        }
    }

    private NPath getConfigFile() {
        return NWorkspace.get()
                .getStoreLocation(NId.ofClass(NIndexSubscriberListManager.class).get(),
                        NStoreType.CONF).resolve(
                        name + "-nuts-subscriber-list.json");
    }

    public List<NIndexSubscriber> getSubscribers() {
        return new ArrayList<>(subscribers.values());
    }

    public NIndexSubscriber getSubscriber(String uuid) {
        NIndexSubscriber subscriber = subscribers.get(uuid);
        if (subscriber == null) {
            throw new NoSuchElementException("subscriber with " + uuid + " does not exist");
        }
        return subscriber.copy();
    }

    public NIndexSubscriberListManager setSubscribers(Map<String, NIndexSubscriber> subscribers) {
        this.subscribers = subscribers;
        return this;
    }

    public NIndexSubscriberListConfig getConfig() {
        return config;
    }

    public NIndexSubscriberListManager setConfig(NIndexSubscriberListConfig config) {
        this.config = config;
        return this;
    }

    public NIndexSubscriber subscribe(String repositoryUuid, NWorkspaceLocation workspaceLocation) {
        if (subscribers.containsKey(repositoryUuid)) {
            subscribers.get(repositoryUuid)
                    .getWorkspaceLocations().put(workspaceLocation.getUuid(), workspaceLocation.copy());
        } else {
            subscribers.put(repositoryUuid, new NIndexSubscriber()
                    .setUuid(repositoryUuid)
                    .setName(getRepositoryNameFromUuid(repositoryUuid))
                    .setWorkspaceLocations(Collections.singletonMap(workspaceLocation.getUuid(), workspaceLocation.copy())));
        }
        this.save();
        return subscribers.get(repositoryUuid);
    }

    private String getRepositoryNameFromUuid(String repositoryUuid) {
        List<NRepository> repositories = NWorkspace.get().getRepositories();
        for (NRepository repository : repositories) {
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
        NPath file = getConfigFile();
        NElements.of().json().setValue(this.config)
                .setNtf(false).print(file);
    }

    public boolean unsubscribe(String repositoryUuid, NWorkspaceLocation workspaceLocation) {
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

    public boolean isSubscribed(String repositoryUuid, NWorkspaceLocation workspaceLocation) {
        return this.subscribers.containsKey(repositoryUuid)
                && this.subscribers.get(repositoryUuid).getWorkspaceLocations().containsKey(workspaceLocation.getUuid());
    }
}
