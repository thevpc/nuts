package net.vpc.toolbox.ntasks;

import java.time.Instant;

public class NTask {
    @Id
    private String id;
    private String name;
    private Instant startTime;
    private NTimePeriod internalDuration;
    private NTimePeriod duration;
    private String project;

    private String observations;

    public String getName() {
        return name;
    }

    public String getProject() {
        return project;
    }

    public NTask setName(String name) {
        this.name = name;
        return this;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public NTask setStartTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public NTimePeriod getInternalDuration() {
        return internalDuration;
    }

    public NTask setInternalDuration(NTimePeriod internalDuration) {
        this.internalDuration = internalDuration;
        return this;
    }

    public NTimePeriod getDuration() {
        return duration;
    }

    public NTask setDuration(NTimePeriod duration) {
        this.duration = duration;
        return this;
    }

    public NTask setProject(String project) {
        this.project = project;
        return this;
    }

    public String getId() {
        return id;
    }

    public NTask setId(String id) {
        this.id = id;
        return this;
    }

    public String getObservations() {
        return observations;
    }

    public NTask setObservations(String observations) {
        this.observations = observations;
        return this;
    }
}
