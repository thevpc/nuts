package net.thevpc.nuts.toolbox.nstore.model;

import javafx.scene.image.Image;

public class PackageInfo {
    private String name;
    private String genericName;
    private String description;
    private int ratings;
    private long ratingsCount;
    private String icon;
    private Image imageIcon;
    private boolean lib;
    private boolean app;
    private boolean napp;
    private boolean gui;
    private boolean term;

    public PackageInfo() {

    }

    public String getName() {
        return name;
    }

    public PackageInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getGenericName() {
        return genericName;
    }

    public PackageInfo setGenericName(String genericName) {
        this.genericName = genericName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PackageInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public int getRatings() {
        return ratings;
    }

    public PackageInfo setRatings(int ratings) {
        this.ratings = ratings;
        return this;
    }

    public long getRatingsCount() {
        return ratingsCount;
    }

    public PackageInfo setRatingsCount(long ratingsCount) {
        this.ratingsCount = ratingsCount;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public PackageInfo setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public Image getImageIcon() {
        return imageIcon;
    }

    public PackageInfo setImageIcon(Image imageIcon) {
        this.imageIcon = imageIcon;
        return this;
    }

    public boolean isLib() {
        return lib;
    }

    public PackageInfo setLib(boolean lib) {
        this.lib = lib;
        return this;
    }

    public boolean isApp() {
        return app;
    }

    public PackageInfo setApp(boolean app) {
        this.app = app;
        return this;
    }

    public boolean isNapp() {
        return napp;
    }

    public PackageInfo setNapp(boolean napp) {
        this.napp = napp;
        return this;
    }

    public boolean isGui() {
        return gui;
    }

    public PackageInfo setGui(boolean gui) {
        this.gui = gui;
        return this;
    }

    public boolean isTerm() {
        return term;
    }

    public PackageInfo setTerm(boolean term) {
        this.term = term;
        return this;
    }

    public PackageInfo(String name, String genericName, String description, int ratings, long ratingsCount, String icon, Image imageIcon, boolean lib, boolean app, boolean napp, boolean gui, boolean term) {
        this.name = name;
        this.genericName = genericName;
        this.description = description;
        this.ratings = ratings;
        this.ratingsCount = ratingsCount;
        this.icon = icon;
        this.imageIcon = imageIcon;
        this.lib = lib;
        this.app = app;
        this.napp = napp;
        this.gui = gui;
        this.term = term;
    }
}
