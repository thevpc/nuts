package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NId;

import java.time.Instant;

public class NInstallLogRecord {
    private Instant date;
    private String user;
    private NInstallLogAction action;
    private NId id;
    private NId forId;
    private boolean succeeded;
    private String message;

    public NInstallLogRecord() {
    }

    public NInstallLogRecord(Instant date, String user, NInstallLogAction action, NId id, NId forId, String message, boolean succeeded) {
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

    public NInstallLogRecord setMessage(String message) {
        this.message = message;
        return this;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public NInstallLogRecord setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
        return this;
    }

    public Instant getDate() {
        return date;
    }

    public NInstallLogRecord setDate(Instant date) {
        this.date = date;
        return this;
    }

    public String getUser() {
        return user;
    }

    public NInstallLogRecord setUser(String user) {
        this.user = user;
        return this;
    }

    public NInstallLogAction getAction() {
        return action;
    }

    public NInstallLogRecord setAction(NInstallLogAction action) {
        this.action = action;
        return this;
    }

    public NId getId() {
        return id;
    }

    public NInstallLogRecord setId(NId id) {
        this.id = id;
        return this;
    }

    public NId getForId() {
        return forId;
    }

    public NInstallLogRecord setForId(NId forId) {
        this.forId = forId;
        return this;
    }
}
