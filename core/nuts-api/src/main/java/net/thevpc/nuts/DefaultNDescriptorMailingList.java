package net.thevpc.nuts;

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
        this(other.getId(), other.getName(), other.getSubscribe(), other.getUnsubscribe(), other.getPost(), other.getArchive(),
                other.getOtherArchives(), other.getProperties(), other.getComments()
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
    public String getId() {
        return id;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSubscribe() {
        return subscribe;
    }

    @Override
    public String getUnsubscribe() {
        return unsubscribe;
    }

    @Override
    public String getPost() {
        return post;
    }

    @Override
    public String getArchive() {
        return archive;
    }

    @Override
    public List<String> getOtherArchives() {
        return otherArchives;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getComments() {
        return comments;
    }

    public NDescriptorMailingList readOnly() {
        return this;
    }

    public NDescriptorMailingListBuilder builder() {
        return new DefaultNDescriptorMailingListBuilder(this);
    }
}
