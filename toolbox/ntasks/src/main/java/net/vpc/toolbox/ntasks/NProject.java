package net.vpc.toolbox.ntasks;

import java.time.Instant;

public class NProject {
    @Id
    private String name;
    private String customer;
    private String company;
    private NDay startWeekDay;
    private Instant startTime;
    private String observations;

    public String getName() {
        return name;
    }

    public NProject setName(String name) {
        this.name = name;
        return this;
    }

    public String getCustomer() {
        return customer;
    }

    public NProject setCustomer(String customer) {
        this.customer = customer;
        return this;
    }

    public String getCompany() {
        return company;
    }

    public NProject setCompany(String company) {
        this.company = company;
        return this;
    }

    public NDay getStartWeekDay() {
        return startWeekDay;
    }

    public NProject setStartWeekDay(NDay startWeekDay) {
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
}
