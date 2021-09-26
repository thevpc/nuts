package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.io.PrintStream;
import java.net.URL;

/**
 * this class implements several utility methods to be used by Nuts API interfaces
 */
public class NutsApiUtils {

    private NutsApiUtils() {
    }
    public static boolean isBlank(CharSequence s) {
        return s == null || isBlank(s.toString().toCharArray());
    }

    public static boolean isBlank(char[] string) {
        if (string == null || string.length == 0) {
            return true;
        }
        for (char c : string) {
            if (c > ' ') {
                return false;
            }
        }
        return true;
    }
    public static void checkSession(NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("missing session");
        }
    }

    public static String[] parseCommandLineArray(String commandLineString) {
        return PrivateNutsCommandLine.parseCommandLineArray(commandLineString);
    }

    public static int processThrowable(Throwable ex, PrintStream out) {
        return PrivateNutsUtilApplication.processThrowable(ex, out);
    }

    public static int processThrowable(Throwable ex, PrintStream out, boolean showMessage, boolean showTrace, boolean showGui) {
        return PrivateNutsUtilApplication.processThrowable(ex, out, showMessage, showTrace, showGui);
    }

    public static boolean isGraphicalDesktopEnvironment() {
        return PrivateNutsUtilGui.isGraphicalDesktopEnvironment();
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return PrivateNutsUtils.getSysBoolNutsProperty(property, defaultValue);
    }

    public static String resolveNutsVersionFromClassPath() {
        return PrivateNutsUtilMaven.resolveNutsApiVersionFromClassPath();
    }

    public static String resolveNutsIdDigest() {
        return resolveNutsIdDigest(
                new NutsBootId("net.thevpc.nuts", "nuts", NutsBootVersion.parse(Nuts.getVersion())),
                PrivateNutsUtilClassLoader.resolveClasspathURLs(Thread.currentThread().getContextClassLoader())
        );
    }

    public static String resolveNutsIdDigest(NutsBootId id, URL[] urls) {
        return PrivateNutsUtilDigest.getURLDigest(
                PrivateNutsUtilClassLoader.findClassLoaderJar(id, urls)
        );
    }

    public static URL findClassLoaderJar(NutsBootId id, URL[] urls) {
        return PrivateNutsUtilClassLoader.findClassLoaderJar(id, urls);
    }


    public static NutsWorkspaceOptionsBuilder createOptionsBuilder() {
        return new PrivateBootWorkspaceOptions();
    }

    public static NutsWorkspaceOptions createOptions() {
        return new PrivateBootWorkspaceOptions();
    }

    /**
     * creates a string key combining layout and location.
     * le key has the form of a concatenated layout and location ids separated by ':'
     * where null layout is replaced by 'system' keyword.
     * used in {@link NutsWorkspaceOptions#getHomeLocations()}.
     *
     * @param storeLocationLayout layout
     * @param location            location
     * @return combination of layout and location separated by ':'.
     */
    public static String createHomeLocationKey(NutsOsFamily storeLocationLayout, NutsStoreLocation location) {
        return (storeLocationLayout == null ? "system" : storeLocationLayout.id()) + ":" + (location == null ? "system" : location.id());
    }

}