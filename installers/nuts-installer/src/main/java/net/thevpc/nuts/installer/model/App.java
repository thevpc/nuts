package net.thevpc.nuts.installer.model;

public class App {
    private String id;
    private String repo;

    public App(String id) {
        this.id = id;
    }

    public App(String id, String repo) {
        this.id = id;
        this.repo = repo;
    }

    public String getId() {
        return id;
    }

    public String getRepo() {
        return repo;
    }
}
