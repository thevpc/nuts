package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamVisitor;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.Pom;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.PomXmlParser;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import net.thevpc.nuts.runtime.standalone.xtra.execentries.DefaultNutsExecutionEntry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;

public class JavaJarUtils {
    public static NutsVersion[] parseJarClassVersions(InputStream jarStream, NutsSession session) {
        if (!(jarStream instanceof BufferedInputStream)) {
            jarStream = new BufferedInputStream(jarStream);
        }
        final HashSet<String> classes = new HashSet<>();
        ZipUtils.visitZipStream(jarStream, new Predicate<String>() {
            @Override
            public boolean test(String path) {
                return path.endsWith(".class");
            }
        }, new InputStreamVisitor() {
            @Override
            public boolean visit(String path, InputStream inputStream) throws IOException {
                JavaClassByteCode.Visitor cl = new JavaClassByteCode.Visitor() {
                    @Override
                    public boolean visitVersion(int major, int minor) {
                        classes.add(JavaClassUtils.classVersionToSourceVersion(major, minor, session));
                        return false;
                    }

                };
                JavaClassByteCode classReader = new JavaClassByteCode(new BufferedInputStream(inputStream), cl, session);
                return true;
            }
        }, session);
        return classes.stream().map(x -> NutsVersion.of(x, session)).toArray(NutsVersion[]::new);
    }

    public static NutsVersion parseJarClassVersion(InputStream jarStream, NutsSession session) {
        NutsVersion[] all = parseJarClassVersions(jarStream, session);
        if (all.length == 0) {
            return null;
        }
        NutsVersion nb = all[0];
        for (int i = 1; i < all.length; i++) {
            if (nb.compareTo(all[i]) < 0) {
                nb = all[i];
            }
        }
        return nb;
    }

    public static NutsExecutionEntry[] parseJarExecutionEntries(InputStream jarStream, String sourceName, NutsSession session) {
        if (!(jarStream instanceof BufferedInputStream)) {
            jarStream = new BufferedInputStream(jarStream);
        }
        final LinkedHashSet<NutsExecutionEntry> classes = new LinkedHashSet<>();
        final List<String> manifestClass = new ArrayList<>();
        ZipUtils.visitZipStream(jarStream, new Predicate<String>() {
            @Override
            public boolean test(String path) {
                return path.endsWith(".class")
                        || path.equals("META-INF/MANIFEST.MF")
                        || (path.startsWith("META-INF/maven/") && path.endsWith("/pom.xml"))
                        ;
            }
        }, new InputStreamVisitor() {
            @Override
            public boolean visit(String path, InputStream inputStream) throws IOException {
                if (path.endsWith(".class")) {
                    NutsExecutionEntry mainClass = JavaClassUtils.parseClassExecutionEntry(inputStream, path, session);
                    if (mainClass != null) {
                        classes.add(mainClass);
                    }
                } else if (path.equals("META-INF/MANIFEST.MF")) {
                    Manifest manifest = new Manifest(inputStream);
                    Attributes a = manifest.getMainAttributes();
                    if (a != null && a.containsKey("Main-Class")) {
                        String v = a.getValue("Main-Class");
                        if (!NutsBlankable.isBlank(v)) {
                            manifestClass.add(v);
                        }
                    }
                } else if (path.startsWith("META-INF/maven/") && path.endsWith("/pom.xml")) {
                    Pom pom = new PomXmlParser(session).parse(inputStream, session);
                    final Element ee = pom.getXml().getDocumentElement();
                    XmlUtils.visitNode(ee, x -> {
                        if (x instanceof Element) {
                            Element e = (Element) x;
                            if (XmlUtils.isNode(e, "build", "plugins", "plugin", "configuration", "archive", "manifest", "mainClass")) {
                                String s = NutsUtilStrings.trim(e.getTextContent());
                                if (s.length() > 0) {
                                    classes.add(new DefaultNutsExecutionEntry(s, true, false));
                                }
                            } else if (XmlUtils.isNode(e, "build", "plugins", "plugin", "executions", "execution", "configuration", "mainClass")) {
                                //              configuration   execution       executions      plugin
                                Node plugin = e.getParentNode().getParentNode().getParentNode().getParentNode();
                                NutsIdBuilder ib = NutsIdBuilder.of(session);
                                for (Node node : XmlUtils.iterable(plugin)) {
                                    Element ne = XmlUtils.asElement(node);
                                    if (ne != null) {
                                        switch (ne.getNodeName()) {
                                            case "groupId": {
                                                ib.setGroupId(NutsUtilStrings.trim(ne.getTextContent()));
                                                break;
                                            }
                                            case "artifactId": {
                                                ib.setArtifactId(NutsUtilStrings.trim(ne.getTextContent()));
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (ib.build().toString().equals("org.openjfx:javafx-maven-plugin")) {
                                    String s = NutsUtilStrings.trim(e.getTextContent());
                                    if (s.length() > 0) {
                                        classes.add(new DefaultNutsExecutionEntry(s, true, false));
                                    }
                                }
                            }
                            return true;
                        }
                        return false;
                    });
                }
                return true;
            }
        }, session);

        List<NutsExecutionEntry> entries = new ArrayList<>();
        String defaultEntry = null;
        if (manifestClass.size() > 0) {
            defaultEntry = manifestClass.get(0);
        }
        boolean defaultFound = false;
        for (NutsExecutionEntry entry : classes) {
            if (defaultEntry != null && defaultEntry.equals(entry.getName())) {
                entries.add(new DefaultNutsExecutionEntry(entry.getName(), true, entry.isApp()));
                defaultFound = true;
            } else {
                entries.add(entry);
            }
        }
        if (defaultEntry != null && !defaultFound) {
            NutsLoggerOp.of(CorePlatformUtils.class, session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("invalid default entry {0} in {1}", defaultEntry, sourceName));
//            entries.add(new DefaultNutsExecutionEntry(defaultEntry, true, false));
        }
        return entries.toArray(new NutsExecutionEntry[0]);
    }

    public static NutsVersion parseJarClassVersion(NutsPath path, NutsSession session) {
        try (InputStream is = path.getInputStream()) {
            return parseJarClassVersion(is, session);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static String parseDefaultModuleName(NutsPath jarStream, NutsSession session) {
        try(InputStream is=jarStream.getInputStream()){
            return parseDefaultModuleName(is,session);
        }catch (IOException ex){
            throw new NutsIOException(session,ex);
        }
    }

    public static String parseDefaultModuleName(InputStream jarStream, NutsSession session) {
        NutsRef<String> automaticModuleName = new NutsRef<>();
        ZipUtils.visitZipStream(jarStream, new Predicate<String>() {
            @Override
            public boolean test(String path) {
                return "META-INF/MANIFEST.MF".equals(path);
            }
        }, new InputStreamVisitor() {
            @Override
            public boolean visit(String path, InputStream inputStream) throws IOException {
                if ("META-INF/MANIFEST.MF".equals(path)) {
                    try {
                        Manifest manifest = new Manifest(inputStream);
                        Attributes attrs = manifest.getMainAttributes();
                        for (Object o : attrs.keySet()) {
                            Attributes.Name attrName = (Attributes.Name) o;
                            if ("Automatic-Module-Name".equals(attrName.toString())) {
                                automaticModuleName.set(NutsUtilStrings.trimToNull(attrs.getValue(attrName)));
                                return false;
                            }
                        }
                    } finally {
                        inputStream.close();
                    }
                }
                return true;
            }
        }, session);
        return automaticModuleName.get();
    }

    public static JavaClassByteCode.ModuleInfo parseModuleInfo(NutsPath jar, NutsSession session) {
        try(InputStream is=jar.getInputStream()){
            return parseModuleInfo(is,session);
        }catch (IOException ex){
            throw new NutsIOException(session,ex);
        }
    }

    public static JavaClassByteCode.ModuleInfo parseModuleInfo(InputStream jarStream, NutsSession session) {
        if (!(jarStream instanceof BufferedInputStream)) {
            jarStream = new BufferedInputStream(jarStream);
        }
        final LinkedHashSet<NutsExecutionEntry> classes = new LinkedHashSet<>();
        NutsRef<JavaClassByteCode.ModuleInfo> ref = new NutsRef<>();
        ZipUtils.visitZipStream(jarStream, path -> path.equals("module-info.class"), new InputStreamVisitor() {
            @Override
            public boolean visit(String path, InputStream inputStream)  {
                JavaClassByteCode s = new JavaClassByteCode(inputStream, new JavaClassByteCode.Visitor() {
                    @Override
                    public boolean visitClassAttributeModule(JavaClassByteCode.ModuleInfo mi) {
                        ref.set(mi);
                        return true;
                    }
                }, session);
                return false;
            }
        }, session);
        return ref.get();
    }
}
