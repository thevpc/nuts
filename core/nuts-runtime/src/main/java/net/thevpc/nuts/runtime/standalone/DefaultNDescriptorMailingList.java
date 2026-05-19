package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.artifact.NDescriptorMailingList;
import net.thevpc.nuts.artifact.NDescriptorMailingListBuilder;

import java.util.*;

public class DefaultNDescriptorMailingList implements NDescriptorMailingList {
    private final String id;
    private final String name;
    private final String subscribe;
    private final String unsubscribe;
    private final String post;
    private final String archive;
    private final List<String> otherArchives;
    private final Map<String, String> properties;
    private final String comments;

    public DefaultNDescriptorMailingList(NDescriptorMailingList other) {
        this(other.id(), other.name(), other.subscribe(), other.unsubscribe(), other.post(), other.archive(),
                other.otherArchives(), other.properties(), other.comments()
        );
    }

    public DefaultNDescriptorMailingList(String id, String name, String subscribe, String unsubscribe, String post, String archive, List<String> otherArchives, Map<String, String> properties, String comments) {
        this.id = id;
        this.name = name;
        this.subscribe = subscribe;
        this.unsubscribe = unsubscribe;
        this.post = post;
        this.archive = archive;
        this.otherArchives = otherArchives == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(otherArchives));
        this.properties = properties == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(properties));
        this.comments = comments;
    }

    @Override
    public String id() {
        return id;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public String subscribe() {
        return subscribe;
    }

    @Override
    public String unsubscribe() {
        return unsubscribe;
    }

    @Override
    public String post() {
        return post;
    }

    @Override
    public String archive() {
        return archive;
    }

    @Override
    public List<String> otherArchives() {
        return otherArchives;
    }

    @Override
    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public String comments() {
        return comments;
    }

    public NDescriptorMailingList readOnly() {
        return this;
    }

    public NDescriptorMailingListBuilder builder() {
        return new DefaultNDescriptorMailingListBuilder(this);
    }
}
