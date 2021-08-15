package net.thevpc.nuts;

/**
 * @app.category Application
 */
public interface NutsAppVersionStoreLocationSupplier {
    String getStoreLocation(NutsStoreLocation folderType, String version);
}
