package net.thevpc.nuts.toolbox.noapi.model;

public class ConfigVar {
    private String id;
    private String name;
    private String description;
    private String example;
    private String value;
    private String observations;

    public ConfigVar(String id, String name, String description, String example, String value, String observations) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.example = example;
        this.value = value;
        this.observations = observations;
    }

    public String getId() {
        return id;
    }

    public ConfigVar setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ConfigVar setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ConfigVar setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getExample() {
        return example;
    }

    public ConfigVar setExample(String example) {
        this.example = example;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ConfigVar setValue(String value) {
        this.value = value;
        return this;
    }

    public String getObservations() {
        return observations;
    }

    public ConfigVar setObservations(String observations) {
        this.observations = observations;
        return this;
    }
}
