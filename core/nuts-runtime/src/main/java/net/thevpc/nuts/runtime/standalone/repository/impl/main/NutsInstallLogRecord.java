package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NutsId;

import java.time.Instant;

public class NutsInstallLogRecord {
    private Instant date;
    private String user;
    private NutsInstallLogAction action;
    private NutsId id;
    private NutsId forId;
    private boolean succeeded;
    private String message;

    public NutsInstallLogRecord() {
    }

    public NutsInstallLogRecord(Instant date, String user, NutsInstallLogAction action, NutsId id, NutsId forId, String message, boolean succeeded) {
        this.date = date;
        this.user = user;
        this.action = action;
        this.id = id;
        this.forId = forId;
        this.succeeded = succeeded;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public NutsInstallLogRecord setMessage(String message) {
        this.message = message;
        return this;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public NutsInstallLogRecord setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
        return this;
    }

    public Instant getDate() {
        return date;
    }

    public NutsInstallLogRecord setDate(Instant date) {
        this.date = date;
        return this;
    }

    public String getUser() {
        return user;
    }

    public NutsInstallLogRecord setUser(String user) {
        this.user = user;
        return this;
    }

    public NutsInstallLogAction getAction() {
        return action;
    }

    public NutsInstallLogRecord setAction(NutsInstallLogAction action) {
        this.action = action;
        return this;
    }

    public NutsId getId() {
        return id;
    }

    public NutsInstallLogRecord setId(NutsId id) {
        this.id = id;
        return this;
    }

    public NutsId getForId() {
        return forId;
    }

    public NutsInstallLogRecord setForId(NutsId forId) {
        this.forId = forId;
        return this;
    }
}
