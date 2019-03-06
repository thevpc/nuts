package net.vpc.app.nuts.indexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NutsIndexSubscriberListConfig implements Serializable {

    private static final long serialVersionUID = 1;

    private String uuid;
    private String name;
    private List<NutsIndexSubscriber> subscribers = new ArrayList<>();

    public NutsIndexSubscriberListConfig() {
    }

    public NutsIndexSubscriberListConfig(NutsIndexSubscriberListConfig other) {
        this.uuid = other.getUuid();
        this.name = other.getName();
        this.subscribers = new ArrayList<>(other.getSubscribers());
    }

    public String getUuid() {
        return uuid;
    }

    public NutsIndexSubscriberListConfig setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public NutsIndexSubscriberListConfig setName(String name) {
        this.name = name;
        return this;
    }

    public List<NutsIndexSubscriber> getSubscribers() {
        return subscribers;
    }

    public NutsIndexSubscriberListConfig setSubscribers(List<NutsIndexSubscriber> subscribers) {
        this.subscribers = subscribers;
        return this;
    }
}
