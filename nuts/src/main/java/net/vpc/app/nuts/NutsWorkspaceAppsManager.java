package net.vpc.app.nuts;

public interface NutsWorkspaceAppsManager {

    /**
     * create a new instance of {@link NutsApplicationContext}
     *
     * @param args            application arguments
     * @param appClass        application class
     * @param storeId         application store id or null
     * @param startTimeMillis application start time
     * @return new instance of {@link NutsApplicationContext}
     */
    NutsApplicationContext createApplicationContext(String[] args, Class appClass, String storeId, long startTimeMillis);

    NutsExecutionEntryManager execEntries();
}
