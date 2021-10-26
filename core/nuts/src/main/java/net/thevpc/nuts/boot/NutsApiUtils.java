package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;

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

    public static String resolveNutsVersionFromClassPath(PrivateNutsLog LOG) {
        return PrivateNutsUtilMaven.resolveNutsApiVersionFromClassPath(LOG);
    }

    public static String resolveNutsIdDigestOrError() {
        String d = resolveNutsIdDigest();
        if (d == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL[] urls = PrivateNutsUtilClassLoader.resolveClasspathURLs(cl,true);
            throw new NutsBootException(
                    NutsMessage.plain(
                            "unable to detect nuts digest. Most likely you are missing valid compilation of nuts." +
                                    "\n\t 'pom.properties' could not be resolved and hence, we are unable to resolve nuts version." +
                                    "\n\t java="+ System.getProperty("java.home")+ " as "+System.getProperty("java.version")+
                                    "\n\t class-path="+ System.getProperty("java.class.path")+
                                    "\n\t urls="+ Arrays.toString(urls)+
                                    "\n\t class-loader="+ cl.getClass().getName()+" as "+cl
                    )
            );
        }
        return d;

    }
    public static String resolveNutsIdDigest() {
        return resolveNutsIdDigest(
                new NutsBootId("net.thevpc.nuts", "nuts", NutsBootVersion.parse(Nuts.getVersion())),
                PrivateNutsUtilClassLoader.resolveClasspathURLs(Thread.currentThread().getContextClassLoader(),true)
        );
    }

    public static String resolveNutsIdDigest(NutsBootId id, URL[] urls) {
        return PrivateNutsUtilDigest.getURLDigest(
                PrivateNutsUtilClassLoader.findClassLoaderJar(id, urls),
                null
        );
    }

    public static URL findClassLoaderJar(NutsBootId id, URL[] urls) {
        return PrivateNutsUtilClassLoader.findClassLoaderJar(id, urls);
    }


    public static NutsWorkspaceOptionsBuilder createOptionsBuilder() {
        PrivateNutsLog lvl = new PrivateNutsLog(null);
        PrivateBootWorkspaceOptions o = new PrivateBootWorkspaceOptions(lvl);
        lvl.setOptions(o);
        return o;
    }

//    /**
//     * creates a string key combining layout and location.
//     * le key has the form of a concatenated layout and location ids separated by ':'
//     * where null layout is replaced by 'system' keyword.
//     * used in {@link NutsWorkspaceOptions#getHomeLocations()}.
//     *
//     * @param storeLocationLayout layout
//     * @param location            location
//     * @return combination of layout and location separated by ':'.
//     */
//    public static String createHomeLocationKey(NutsOsFamily storeLocationLayout, NutsStoreLocation location) {
//        return (storeLocationLayout == null ? "system" : storeLocationLayout.id()) + ":" + (location == null ? "system" : location.id());
//    }

    public static <T extends NutsEnum> void checkNonNullEnum(T objectValue, String stringValue, Class<T> enumType, NutsSession session) {
        if (objectValue == null) {
            if (!NutsBlankable.isBlank(stringValue)) {
                if (session == null) {
                    throw new NutsBootException(NutsMessage.cstyle("invalid value %s of type %s", stringValue, enumType.getName()));
                }
                throw new NutsParseEnumException(session, stringValue, NutsCommandLineFormatStrategy.class);
            }
        }
    }

    public static Level parseLenientLogLevel(String value, Level emptyValue, Level errorValue) {
        return PrivateNutsUtils.parseLenientLogLevel(value, emptyValue, errorValue);
    }

    public static Integer parseInt(String value, Integer emptyValue, Integer errorValue) {
        return PrivateNutsUtils.parseInt(value, emptyValue, errorValue);
    }

    public static Integer parseFileSizeInBytes(String value, Integer defaultMultiplier, Integer emptyValue, Integer errorValue) {
        return PrivateNutsUtils.parseFileSizeInBytes(value, defaultMultiplier, emptyValue, errorValue);
    }
}