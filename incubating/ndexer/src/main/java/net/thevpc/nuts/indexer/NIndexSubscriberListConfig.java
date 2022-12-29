package net.thevpc.nuts.indexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NIndexSubscriberListConfig implements Serializable {

    private static final long serialVersionUID = 1;

    private String uuid;
    private String name;
    private List<NIndexSubscriber> subscribers = new ArrayList<>();

    public NIndexSubscriberListConfig() {
    }

    public NIndexSubscriberListConfig(NIndexSubscriberListConfig other) {
        this.uuid = other.getUuid();
        this.name = other.getName();
        this.subscribers = new ArrayList<>(other.getSubscribers());
    }

    public String getUuid() {
        return uuid;
    }

    public NIndexSubscriberListConfig setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public NIndexSubscriberListConfig setName(String name) {
        this.name = name;
        return this;
    }

    public List<NIndexSubscriber> getSubscribers() {
        return subscribers;
    }

    public NIndexSubscriberListConfig setSubscribers(List<NIndexSubscriber> subscribers) {
        this.subscribers = subscribers;
        return this;
    }
}
