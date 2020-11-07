package net.thevpc.nuts.runtime.util;

public class InstalledVsNonInstalledSearch {
    private boolean searchInInstalled;
    private boolean searchInOtherRepositories;

    public InstalledVsNonInstalledSearch(boolean searchInInstalled, boolean searchInOtherRepositories) {
        this.searchInInstalled = searchInInstalled;
        this.searchInOtherRepositories = searchInOtherRepositories;
    }

    public boolean isSearchInInstalled() {
        return searchInInstalled;
    }

    public boolean isSearchInOtherRepositories() {
        return searchInOtherRepositories;
    }
}
