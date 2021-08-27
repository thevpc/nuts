package net.thevpc.nuts.boot;

class PrivateNutsErrorInfo {
    private final String nutsId;
    private final String repository;
    private final String url;
    private final String message;
    private final Throwable throwable;

    public PrivateNutsErrorInfo(String nutsId, String repository, String url, String message, Throwable throwable) {
        this.nutsId = nutsId;
        this.repository = repository;
        this.url = url;
        this.message = message;
        this.throwable = throwable;
    }

    public String getNutsId() {
        return nutsId;
    }

    public String getRepository() {
        return repository;
    }

    public String getUrl() {
        return url;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return getMessage() + " " + getNutsId() + " from " + getUrl() + " (repository " + getRepository() + ") : "
                + (getThrowable() == null ? "" : getThrowable().toString());
    }
}
