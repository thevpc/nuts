package net.vpc.app.nuts.toolbox.njob.model;

import net.vpc.app.nuts.toolbox.njob.time.TimePeriod;

import java.time.Instant;

public class NJob {
    @Id
    private String id;
    private String name;
    private Instant startTime;
    private TimePeriod internalDuration;
    private TimePeriod duration;
    private String project;
    private Instant creationTime;
    private Instant modificationTime;

    private String observations;

    public String getName() {
        return name;
    }

    public String getProject() {
        return project;
    }

    public NJob setName(String name) {
        this.name = name;
        return this;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public NJob setStartTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public TimePeriod getInternalDuration() {
        return internalDuration;
    }

    public NJob setInternalDuration(TimePeriod internalDuration) {
        this.internalDuration = internalDuration;
        return this;
    }

    public TimePeriod getDuration() {
        return duration;
    }

    public NJob setDuration(TimePeriod duration) {
        this.duration = duration;
        return this;
    }

    public NJob setProject(String project) {
        this.project = project;
        return this;
    }

    public String getId() {
        return id;
    }

    public NJob setId(String id) {
        this.id = id;
        return this;
    }

    public String getObservations() {
        return observations;
    }

    public NJob setObservations(String observations) {
        this.observations = observations;
        return this;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public NJob setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public Instant getModificationTime() {
        return modificationTime;
    }

    public NJob setModificationTime(Instant modificationTime) {
        this.modificationTime = modificationTime;
        return this;
    }
}
