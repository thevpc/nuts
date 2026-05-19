package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.artifact.NDescriptorMailingList;
import net.thevpc.nuts.artifact.NDescriptorMailingListBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultNDescriptorMailingListBuilder implements NDescriptorMailingListBuilder {
    private String id;
    private String name;
    private String subscribe;
    private String unsubscribe;
    private String post;
    private String archive;
    private List<String> otherArchives;
    private Map<String, String> properties;
    private String comments;

    public DefaultNDescriptorMailingListBuilder() {
        this.otherArchives = new ArrayList<>();
        this.properties = new LinkedHashMap<>();
    }

    public DefaultNDescriptorMailingListBuilder(NDescriptorMailingList other) {
        this(other.id(), other.name(), other.subscribe(), other.unsubscribe(), other.post(), other.archive(),
                other.otherArchives(), other.properties(), other.comments()
        );
    }

    public DefaultNDescriptorMailingListBuilder(String id, String name, String subscribe, String unsubscribe, String post, String archive, List<String> otherArchives, Map<String, String> properties, String comments) {
        this.id = id;
        this.name = name;
        this.subscribe = subscribe;
        this.unsubscribe = unsubscribe;
        this.post = post;
        this.archive = archive;
        this.otherArchives = otherArchives == null ? new ArrayList<>() : new ArrayList<>(otherArchives);
        this.properties = properties == null ? new LinkedHashMap<>() : new LinkedHashMap<>(properties);
        this.comments = comments;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public NDescriptorMailingListBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public NDescriptorMailingListBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String subscribe() {
        return subscribe;
    }

    @Override
    public NDescriptorMailingListBuilder subscribe(String subscribe) {
        this.subscribe = subscribe;
        return this;
    }

    @Override
    public String unsubscribe() {
        return unsubscribe;
    }

    @Override
    public NDescriptorMailingListBuilder unsubscribe(String unsubscribe) {
        this.unsubscribe = unsubscribe;
        return this;
    }

    @Override
    public String post() {
        return post;
    }

    @Override
    public NDescriptorMailingListBuilder post(String post) {
        this.post = post;
        return this;
    }

    @Override
    public String archive() {
        return archive;
    }

    @Override
    public NDescriptorMailingListBuilder archive(String archive) {
        this.archive = archive;
        return this;
    }

    @Override
    public List<String> otherArchives() {
        return otherArchives;
    }

    @Override
    public NDescriptorMailingListBuilder otherArchives(List<String> otherArchives) {
        this.otherArchives = otherArchives;
        return this;
    }

    @Override
    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public NDescriptorMailingListBuilder properties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public String comments() {
        return comments;
    }

    @Override
    public NDescriptorMailingListBuilder comments(String comments) {
        this.comments = comments;
        return this;
    }

    @Override
    public NDescriptorMailingList readOnly() {
        return new DefaultNDescriptorMailingList(this);
    }

    @Override
    public NDescriptorMailingListBuilder builder() {
        return new DefaultNDescriptorMailingListBuilder(this);
    }

    @Override
    public NDescriptorMailingList build() {
        return readOnly();
    }

    @Override
    public NDescriptorMailingListBuilder copy() {
        return builder();
    }
}
