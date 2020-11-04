package net.thevpc.nuts.toolbox.njob.model;

import net.thevpc.nuts.toolbox.njob.time.WeekDay;

import java.time.Instant;
import java.util.Objects;

public class NProject {
    @Id
    private String id;
    private String name;
    private String beneficiary;
    private String company;
    private WeekDay startWeekDay;
    private Instant startTime;
    private Instant creationTime;
    private Instant modificationTime;
    private String observations;

    public String getName() {
        return name;
    }

    public NProject setName(String name) {
        this.name = name;
        return this;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    public NProject setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
        return this;
    }

    public String getCompany() {
        return company;
    }

    public NProject setCompany(String company) {
        this.company = company;
        return this;
    }

    public WeekDay getStartWeekDay() {
        return startWeekDay;
    }

    public NProject setStartWeekDay(WeekDay startWeekDay) {
        this.startWeekDay = startWeekDay;
        return this;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public NProject setStartTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getObservations() {
        return observations;
    }

    public NProject setObservations(String observations) {
        this.observations = observations;
        return this;
    }

    public String getId() {
        return id;
    }

    public NProject setId(String id) {
        this.id = id;
        return this;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public NProject setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public Instant getModificationTime() {
        return modificationTime;
    }

    public NProject setModificationTime(Instant modificationTime) {
        this.modificationTime = modificationTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NProject nProject = (NProject) o;
        return Objects.equals(id, nProject.id) && Objects.equals(name, nProject.name) && Objects.equals(beneficiary, nProject.beneficiary) && Objects.equals(company, nProject.company) && startWeekDay == nProject.startWeekDay && Objects.equals(startTime, nProject.startTime) && Objects.equals(creationTime, nProject.creationTime) && Objects.equals(modificationTime, nProject.modificationTime) && Objects.equals(observations, nProject.observations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, beneficiary, company, startWeekDay, startTime, creationTime, modificationTime, observations);
    }
}
