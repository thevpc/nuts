package net.thevpc.nuts;

/**
 * @category Application
 */
public interface NutsAppVersionStoreLocationSupplier {
    String getStoreLocation(NutsStoreLocation folderType, String version);
}
