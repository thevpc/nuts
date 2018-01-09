package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.Main;
import net.vpc.app.nuts.util.StringUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by vpc on 5/16/17.
 */
public class CorePlatformUtils {
    public static final Map<String, String> SUPPORTED_ARCH_ALIASES = new HashMap<>();
    private static final Set<String> SUPPORTED_ARCH = new HashSet<>(Arrays.asList("x86", "ia64", "amd64", "ppc", "sparc"));
    private static final Set<String> SUPPORTED_OS = new HashSet<>(Arrays.asList("linux", "windows", "mac", "sunos", "freebsd"));
    private static Map<String, String> LOADED_OS_DIST_MAP = null;

    static {
        SUPPORTED_ARCH_ALIASES.put("i386", "x86");
    }

    public static <T> List<T> toList(Iterator<T> it) {
        List<T> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    public static Map<String, String> getOsDistMap() {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.startsWith("linux")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux();
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        if (property.startsWith("mac")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux();
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        if (property.startsWith("sunos")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux();
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        if (property.startsWith("freebsd")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux();
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        return new HashMap<>();
    }

    /**
     * this is inspired from
     * http://stackoverflow.com/questions/15018474/getting-linux-distro-from-java
     * so thanks //PbxMan//
     *
     * @return
     */
    public static Map<String, String> getOsDistMapLinux() {
        File dir = CoreIOUtils.createFileByCwd("/etc/",null);
        List<File> fileList = new ArrayList<>();
        if (dir.exists()) {
            File[] a = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.endsWith("-release");
                }
            });
            if (a != null) {
                fileList.addAll(Arrays.asList(a));
            }
        }
        File fileVersion = CoreIOUtils.createFileByCwd("/proc/version",null);
        if (fileVersion.exists()) {
            fileList.add(fileVersion);
        }
        String disId = null;
        String disName = null;
        String disVersion = null;
        File linuxOsrelease = CoreIOUtils.createFileByCwd("/proc/sys/kernel/osrelease",null);
        StringBuilder osVersion = new StringBuilder();
        if (linuxOsrelease.isFile()) {
            BufferedReader myReader = null;
            String strLine = null;
            try {
                try {
                    myReader = new BufferedReader(new FileReader(linuxOsrelease));
                    while ((strLine = myReader.readLine()) != null) {
                        osVersion.append(strLine).append("\n");
                    }
                } finally {
                    if(myReader!=null) {
                        myReader.close();
                    }
                }
            } catch (IOException e) {
                //ignore
            }
        }
        if (osVersion.toString().trim().isEmpty()) {
            StringUtils.clear(osVersion);
            try {
                CoreIOUtils.execAndEcho(new String[]{"uname", "-r"}, null, null, osVersion, null, 50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//prints all the version-related files
        for (File f : fileList) {
            try {
                BufferedReader myReader = new BufferedReader(new FileReader(f));
                String strLine = null;
                while ((strLine = myReader.readLine()) != null) {
                    strLine = strLine.trim();
                    if (!strLine.startsWith("#") && strLine.contains("=")) {
                        int i = strLine.indexOf('=');
                        String n = strLine.substring(0, i);
                        String v = strLine.substring(i + 1);
                        if (n.equals("ID")) {
                            if (v.startsWith("\"")) {
                                v = v.substring(1, v.length() - 1);
                            }
                            disId = v;
                        } else if (n.equals("VERSION_ID")) {
                            if (v.startsWith("\"")) {
                                v = v.substring(1, v.length() - 1);
                            }
                            disVersion = v;
                        } else if (n.equals("PRETTY_NAME")) {
                            if (v.startsWith("\"")) {
                                v = v.substring(1, v.length() - 1);
                            }
                            disName = v;
                        } else if (n.equals("DISTRIB_ID")) {
                            if (v.startsWith("\"")) {
                                v = v.substring(1, v.length() - 1);
                            }
                            disName = v;
                        } else if (n.equals("DISTRIB_RELEASE")) {
                            if (v.startsWith("\"")) {
                                v = v.substring(1, v.length() - 1);
                            }
                            disVersion = v;
                        }
                        if (!StringUtils.isEmpty(disVersion) && !StringUtils.isEmpty(disName) && !StringUtils.isEmpty(disId)) {
                            break;
                        }
//                        System.out.println(f.getName() + " : " + strLine);
                    }
                }
                myReader.close();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        Map<String, String> m = new HashMap<>();
        m.put("distId", disId);
        m.put("distName", disName);
        m.put("distVersion", disVersion);
        m.put("osVersion", osVersion.toString().trim());
        return m;
    }

    public static String getOsdist() {
        String osInfo = getOs();
        if (osInfo.startsWith("linux")) {
            Map<String, String> m = getOsDistMap();
            String distId = m.get("distId");
            String distVersion = m.get("distVersion");
            if (!StringUtils.isEmpty(distId)) {
                if (!StringUtils.isEmpty(distId)) {
                    return distId + "#" + distVersion;
                } else {
                    return distId;
                }
            }
        }
        return null;
    }

    /**
     * https://en.wikipedia.org/wiki/List_of_Microsoft_Windows_versions
     *
     * @return
     */
    public static String getOs() {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.startsWith("linux")) {
            Map<String, String> m = getOsDistMap();

            String v = m.get("osVersion");
            if (StringUtils.isEmpty(v)) {
                return "linux";
            }
            return "linux#" + v;
        }
        if (property.startsWith("win")) {
            if (property.startsWith("windows 10")) {
                return "windows#10";
            }
            if (property.startsWith("windows 8.1")) {
                return "windows#6.3";
            }
            if (property.startsWith("windows 8")) {
                return "windows#6.2";
            }
            if (property.startsWith("windows 7")) {
                return "windows#6.1";
            }
            if (property.startsWith("windows vista")) {
                return "windows#6";
            }
            if (property.startsWith("windows xp pro")) {
                return "windows#5.2";
            }
            if (property.startsWith("windows xp")) {
                return "windows#5.1";
            }
            return "windows";
        }
        if (property.startsWith("mac")) {
            if (property.startsWith("mac os x")) {
                return "mac#10";
            }
            return "mac";
        }
        if (property.startsWith("sunos")) {
            Map<String, String> m = getOsDistMap();

            String v = m.get("osVersion");
            if (StringUtils.isEmpty(v)) {
                return "sunos";
            }
            return "sunos#" + v;
        }
        if (property.startsWith("freebsd")) {
            Map<String, String> m = getOsDistMap();

            String v = m.get("osVersion");
            if (StringUtils.isEmpty(v)) {
                return "freebsd";
            }
            return "freebsd#" + v;
        }
        return property;
    }

    public static boolean checkSupportedArch(String arch) {
        if (StringUtils.isEmpty(arch)) {
            return true;
        }
        if (SUPPORTED_ARCH.contains(arch)) {
            return true;
        }
        throw new IllegalArgumentException("Unsupported Architecture " + arch + " please do use one of " + SUPPORTED_ARCH);
    }

    public static boolean checkSupportedOs(String os) {
        if (StringUtils.isEmpty(os)) {
            return true;
        }
        if (SUPPORTED_OS.contains(os)) {
            return true;
        }
        throw new IllegalArgumentException("Unsupported Operating System " + os + " please do use one of " + SUPPORTED_OS);
    }

    public static String getArch() {
        String property = System.getProperty("os.arch");
        String aliased = SUPPORTED_ARCH_ALIASES.get(property);
        return (aliased == null) ? property : aliased;
    }

    public static String[] subArray(String[] source, int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            beginIndex = 0;
        }
        if (endIndex > source.length) {
            beginIndex = 0;
        }
        if (beginIndex >= endIndex) {
            return new String[0];
        }
        String[] arr = new String[endIndex - beginIndex];
        System.arraycopy(source, beginIndex, arr, 0, endIndex - beginIndex);
        return arr;
    }
    public static void main(String[] args) {
//        try {
//            System.out.println(resolveLocalFileFromResource(Main.class,"/META-INF/nuts-version.properties"));
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
        try {
            System.out.println(resolveLocalFileFromResource(Main.class,"/java/lang/Object.class"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static File resolveLocalFileFromResource(Class cls,String url) throws MalformedURLException {
        return resolveLocalFileFromURL(resolveURLFromResource(cls,url));
    }


    public static File resolveLocalFileFromURL(URL url){
        try {
            return new File(url.toURI());
        } catch(URISyntaxException e) {
            return new File(url.getPath());
        }
    }

    public static URL resolveURLFromResource(Class cls,String urlPath) throws MalformedURLException {
        if(!urlPath.startsWith("/")){
            throw new IllegalArgumentException("Unable to resolve url from "+urlPath);
        }
        URL url=cls.getResource(urlPath);
        String urlFile = url.getFile();
        int separatorIndex = urlFile.indexOf("!/");
        if (separatorIndex != -1) {
            String jarFile = urlFile.substring(0, separatorIndex);
            try {
                return new URL(jarFile);
            } catch (MalformedURLException ex) {
                // Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
                // This usually indicates that the jar file resides in the file system.
                if (!jarFile.startsWith("/")) {
                    jarFile = "/" + jarFile;
                }
                return new URL("file:" + jarFile);
            }
        } else {
            String encoded =encodePath(urlPath);
            String url_tostring = url.toString();
            if(url_tostring.endsWith(encoded)){
                return new URL(url_tostring.substring(0,url_tostring.length()-encoded.length()));
            }
            throw new IllegalArgumentException("Unable to resolve url from "+urlPath);
        }
    }

    private static String encodePath(String path){
        StringTokenizer st=new StringTokenizer(path,"/",true);
        StringBuilder encoded=new StringBuilder();
        while(st.hasMoreTokens()){
            String t = st.nextToken();
            if(t.equals("/")){
                encoded.append(t);
            }else{
                try {
                    encoded.append(URLEncoder.encode(t, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    throw new IllegalArgumentException("Unable to encode "+t,ex);
                }
            }
        }
        return encoded.toString();
    }

    public static <K,V> Map<K,V> mergeMaps(Map<K,V> source,Map<K,V> dest){
        if(dest==null){
            dest=new HashMap<>();
        }
        if(source!=null) {
            for (Map.Entry<K, V> e : source.entrySet()) {
                if (e.getValue() != null) {
                    dest.put(e.getKey(), e.getValue());
                } else {
                    dest.remove(e.getKey());
                }
            }
        }
        return dest;
    }

}
