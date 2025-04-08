package net.thevpc.nuts.tomcatclassloader;

import net.thevpc.nuts.*;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NutsTomcatClassLoader extends WebappClassLoader {

    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final String SERVICES_PREFIX = "/META-INF/services/";
    private static final org.apache.juli.logging.Log log
            = org.apache.juli.logging.LogFactory.getLog(WebappClassLoaderBase.class);
    protected NWorkspace workspace;
    protected ClassLoader nutsClassLoader;
    protected boolean nutsClassLoaderUnderConstruction;
    protected String nutsPath;
    protected String workspaceLocation;
    protected NId workspaceBootRuntime;
    protected String workspaceExcludedRepositories;
    protected String workspaceExcludedExtensions;
    protected String workspaceArchetype;

    public NutsTomcatClassLoader() {
    }

    public NutsTomcatClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public URL[] getURLs() {
        List<URL> all = new ArrayList<>(Arrays.asList(super.getURLs()));
        ClassLoader classLoader = resolveNutsClassLoader();
        if (classLoader instanceof URLClassLoader) {
            all.addAll(Arrays.asList(((URLClassLoader) classLoader).getURLs()));
        }
        return all.toArray(new URL[0]);
    }

    @Override
    public void setResources(WebResourceRoot resources) {
        super.setResources(resources);
        if (resources != null) {
            WebResource resource = resources.getResource("/META-INF/context.xml");
            if (resource != null) {
                URL url = resource.getURL();
                if (url != null) {
                    try {
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        try (InputStream is = resource.getURL().openStream()) {
                            Document doc = db.parse(is);
                            NodeList loader = doc.getDocumentElement().getElementsByTagName("Loader");
                            if (loader != null) {
                                boolean ok = false;
                                for (int i = 0; i < loader.getLength(); i++) {
                                    Node n = loader.item(i);
                                    Node loaderClass = n.getAttributes().getNamedItem("loaderClass");
                                    if (loaderClass != null) {
                                        String nodeValue = loaderClass.getNodeValue();
                                        if ("net.thevpc.nuts.tomcatclassloader.NutsTomcatClassLoader".equals(nodeValue)) {
                                            ok = true;
                                        }
                                    }
                                    if (ok) {
                                        for (Field field : NutsTomcatClassLoader.class.getDeclaredFields()) {
                                            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                                                Node namedItem = n.getAttributes().getNamedItem(field.getName());
                                                if (namedItem != null) {
                                                    field.setAccessible(true);
                                                    field.set(this, namedItem.getNodeValue());
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public synchronized ClassLoader resolveNutsClassLoader() {
        if (nutsClassLoader == null) {
            nutsClassLoaderUnderConstruction = true;
            try {
                String nutsPath = getNutsPath();
                String[] pathList = splitString(nutsPath, "; ,");
                try {
                    resolveWorkspace().runWith(()->{
                        nutsClassLoader = NSearchCmd.of().addIds(pathList).setInlineDependencies(true).getResultClassLoader();
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    nutsClassLoader = Thread.currentThread().getContextClassLoader();
                }
            } finally {
                nutsClassLoaderUnderConstruction = false;
            }
        }
        return nutsClassLoader;
    }

    public synchronized NWorkspace resolveWorkspace() {
        if (workspace == null) {
            workspace
                    = Nuts.openWorkspace(
                        NWorkspaceOptionsBuilder.of()
                                    .setRuntimeId(getWorkspaceBootRuntime())
                                    .setClassLoaderSupplier(this::getParent)
                                    .setOpenMode(NOpenMode.OPEN_OR_CREATE)
                                    .setWorkspace(getWorkspaceLocation())
                                    .setArchetype(getWorkspaceArchetype())
//                                    .setExcludedRepositories(splitString(getWorkspaceExcludedRepositories(), ";"))
                                    .setExcludedExtensions(
                                            Arrays.asList(splitString(getWorkspaceExcludedExtensions(), " ;"))
                                    ).build()
                    );
        }
        return workspace;
    }

//    @Override
//    protected ResourceEntry findResourceInternal(final String name, final String path) {
//        ResourceEntry r = super.findResourceInternal(name, path);
//        if (r != null) {
//            return r;
//        }
//        boolean isClassResource = path.endsWith(CLASS_FILE_SUFFIX);
//        boolean isCacheable = isClassResource;
//        if (!isCacheable) {
//            isCacheable = path.startsWith(SERVICES_PREFIX);
//        }
//
////        WebResource resource = null;
//        boolean fileNeedConvert = false;
//        if (nutsClassLoaderUnderConstruction) {
//            return null;
//        }
//        ClassLoader classLoader = resolveNutsClassLoader();
//        URL url = classLoader.getResource(name);
//        if (url == null) {
//            return null;
//        }
//
//        File file = resolveFileCodeBase(url);
//
//        ResourceEntry entry = new ResourceEntry();
//        entry.source = url;
//        try {
//            entry.codeBase = file.toURI().toURL();
//        } catch (MalformedURLException e) {
//            return null;
//        }
//        entry.lastModified = file.lastModified();
//
//        if (needConvert && path.endsWith(".properties")) {
//            fileNeedConvert = true;
//        }
//
//        /* Only cache the binary content if there is some content
//         * available one of the following is true:
//         * a) It's a class file since the binary content is only cached
//         *    until the class has been loaded
//         *    or
//         * b) The file needs conversion to address encoding issues (see
//         *    below)
//         *    or
//         * c) The resource is a service provider configuration file located
//         *    under META=INF/services
//         *
//         * In all other cases do not cache the content to prevent
//         * excessive memory usage if large resources are present (see
//         * https://bz.apache.org/bugzilla/show_bug.cgi?id=53081).
//         */
//        if (isCacheable || fileNeedConvert) {
//            byte[] binaryContent = loadFileBytes(entry.source);
//            if (binaryContent != null) {
//                if (fileNeedConvert) {
//                    // Workaround for certain files on platforms that use
//                    // EBCDIC encoding, when they are read through FileInputStream.
//                    // See commit message of rev.303915 for details
//                    // http://svn.apache.org/viewvc?view=revision&revision=303915
//                    String str = new String(binaryContent);
//                    try {
//                        binaryContent = str.getBytes(StandardCharsets.UTF_8);
//                    } catch (Exception e) {
//                        return null;
//                    }
//                }
//                entry.binaryContent = binaryContent;
//                // The certificates and manifest are made available as a side
//                // effect of reading the binary content
//                entry.certificates = new Certificate[0]; //resource.getCertificates();
//            }
//        }
//        entry.manifest = null;// resource.getManifest();
//
//        List<ClassFileTransformer> transformers = getTransformers();
//        if (isClassResource && entry.binaryContent != null
//                && transformers.size() > 0) {
//            // If the resource is a class just being loaded, decorate it
//            // with any attached transformers
//            String className = name.endsWith(CLASS_FILE_SUFFIX)
//                    ? name.substring(0, name.length() - CLASS_FILE_SUFFIX.length()) : name;
//            String internalName = className.replace(".", "/");
//
//            for (ClassFileTransformer transformer : transformers) {
//                try {
//                    byte[] transformed = transformer.transform(
//                            this, internalName, null, null, entry.binaryContent
//                    );
//                    if (transformed != null) {
//                        entry.binaryContent = transformed;
//                    }
//                } catch (IllegalClassFormatException e) {
//                    log.error(sm.getString("webappClassLoader.transformError", name), e);
//                    return null;
//                }
//            }
//        }
//
//        // Add the entry in the local resource repository
//        synchronized (resourceEntries) {
//            // Ensures that all the threads which may be in a race to load
//            // a particular class all end up with the same ResourceEntry
//            // instance
//            ResourceEntry entry2 = resourceEntries.get(path);
//            if (entry2 == null) {
//                resourceEntries.put(path, entry);
//            } else {
//                entry = entry2;
//            }
//        }
//
//        return entry;
//    }

    private String[] splitString(String nutsPath, String sep) {
        List<String> all = new ArrayList<>();
        for (String s : (nutsPath == null ? "" : nutsPath).split("[" + sep + "]")) {
            s = s.trim();
            if (s.length() > 0) {
                all.add(s);
            }
        }
        return all.toArray(new String[0]);
    }
//
//    private File resolveFileCodeBase(URL url) {
//        String urlFile = url.getFile();
//        int separatorIndex = urlFile.indexOf("!/");
//        if (separatorIndex != -1) {
//            String jarFile = urlFile.substring(0, separatorIndex);
//            if (jarFile.startsWith("jar:") && !jarFile.contains("!/")) {
//                return new File(jarFile.substring("jar:".length()));
//            }
//
//            try {
//                return resolveFileCodeBase(new URL(jarFile));
//            } catch (MalformedURLException ex) {
//                // Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
//                // This usually indicates that the jar file resides in the file system.
//                if (!jarFile.startsWith("/")) {
//                    jarFile = "/" + jarFile;
//                }
//                return new File(jarFile);
//            }
//        } else {
//            throw new NutsIllegalArgumentException(nutsWorkspace, "Unable to resolve url from " + urlFile);
//        }
//    }

//    protected byte[] loadFileBytes(URL url) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        InputStream is = null;
//        try {
//            is = url.openStream();
//            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
//            int n;
//
//            while ((n = is.read(byteChunk)) > 0) {
//                baos.write(byteChunk, 0, n);
//            }
//        } catch (IOException e) {
//            log.error("failed while reading bytes from " + url.toExternalForm() + " : " + e.getMessage());
//        } finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    log.error("failed while closing " + url.toExternalForm() + " : " + e.getMessage());
//                }
//            }
//        }
//        return baos.toByteArray();
//    }
//
//    protected List<ClassFileTransformer> getTransformers() {
//        try {
//            Field transformers = super.getClass().getDeclaredField("transformers");
//            transformers.setAccessible(true);
//            return (List<ClassFileTransformer>) transformers.get(this);
//        } catch (Exception ex) {
//            return new ArrayList<>();
//        }
//    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    public String getWorkspaceLocation() {
        return workspaceLocation;
    }

    public void setWorkspaceLocation(String workspaceLocation) {
        this.workspaceLocation = workspaceLocation;
    }

    public NId getWorkspaceBootRuntime() {
        return workspaceBootRuntime;
    }

    public void setWorkspaceBootRuntime(NId workspaceBootRuntime) {
        this.workspaceBootRuntime = workspaceBootRuntime;
    }

    public String getWorkspaceExcludedRepositories() {
        return workspaceExcludedRepositories;
    }

    public void setWorkspaceExcludedRepositories(String workspaceExcludedRepositories) {
        this.workspaceExcludedRepositories = workspaceExcludedRepositories;
    }

    public String getWorkspaceExcludedExtensions() {
        return workspaceExcludedExtensions;
    }

    public void setWorkspaceExcludedExtensions(String workspaceExcludedExtensions) {
        this.workspaceExcludedExtensions = workspaceExcludedExtensions;
    }

    public String getWorkspaceArchetype() {
        return workspaceArchetype;
    }

    public void setWorkspaceArchetype(String workspaceArchetype) {
        this.workspaceArchetype = workspaceArchetype;
    }

    public String getNutsPath() {
        return nutsPath;
    }

    public void setNutsPath(String nutsPath) {
        this.nutsPath = nutsPath;
    }

}
