package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NVisitResult;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPom;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.NPomXmlParser;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import net.thevpc.nuts.runtime.standalone.xtra.execentries.DefaultNExecutionEntry;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JavaJarUtils {


    public static NVersion[] parseJarClassVersions(InputStream jarStream, NSession session) {
        if (!(jarStream instanceof BufferedInputStream)) {
            jarStream = new BufferedInputStream(jarStream);
        }
        final HashSet<String> classes = new HashSet<>();
        ZipUtils.visitZipStream(jarStream, (path, inputStream) -> {
            if (path.endsWith(".class")) {
                JavaClassByteCode.Visitor cl = new JavaClassByteCode.Visitor() {
                    @Override
                    public NVisitResult visitVersion(int major, int minor) {
                        classes.add(JavaClassUtils.classVersionToSourceVersion(major, minor, session));
                        return NVisitResult.TERMINATE;
                    }

                };
                JavaClassByteCode classReader = new JavaClassByteCode(new BufferedInputStream(inputStream), cl, session);
            }
            return NVisitResult.CONTINUE;
        }, session);
        return classes.stream().map(x -> NVersion.of(x).get(session)).toArray(NVersion[]::new);
    }

    public static NVersion parseJarClassVersion(InputStream jarStream, NSession session) {
        NVersion[] all = parseJarClassVersions(jarStream, session);
        if (all.length == 0) {
            return null;
        }
        NVersion nb = all[0];
        for (int i = 1; i < all.length; i++) {
            if (nb.compareTo(all[i]) < 0) {
                nb = all[i];
            }
        }
        return nb;
    }

    public static List<NExecutionEntry> parseJarExecutionEntries(InputStream jarStream, NSession session) {
        if (!(jarStream instanceof BufferedInputStream)) {
            jarStream = new BufferedInputStream(jarStream);
        }
        final LinkedHashSet<NExecutionEntry> classes = new LinkedHashSet<>();
        ZipUtils.visitZipStream(jarStream, (path, inputStream) -> {
            if (path.endsWith(".class")) {
                NExecutionEntry mainClass = JavaClassUtils.parseClassExecutionEntry(inputStream, path, session);
                if (mainClass != null) {
                    classes.add(mainClass);
                }
            } else if (path.equals("META-INF/MANIFEST.MF")) {
                Manifest manifest = new Manifest(inputStream);
                Attributes a = manifest.getMainAttributes();
                if (a != null) {
                    String v = a.getValue("Main-Class");
                    if (!NBlankable.isBlank(v)) {
                        v = NStringUtils.trim(v);
                        classes.add(new DefaultNExecutionEntry(v, true, false));
                    }
                }
            } else if (path.startsWith("META-INF/maven/") && path.endsWith("/pom.xml")) {
                NPom pom = new NPomXmlParser(session).parse(inputStream, session);
                final Element ee = pom.getXml().getDocumentElement();
                if (pom.getParent() != null && pom.getParent().getArtifactId().equals("spring-boot-starter-parent")) {
                    String springStartClass = NStringUtils.trim(pom.getProperties().get("start-class"));
                    if (springStartClass.length() > 0) {
                        classes.add(new DefaultNExecutionEntry(springStartClass, true, false));
                    }
                }
                XmlUtils.visitNode(ee, x -> {
                    if (x instanceof Element) {
                        Element e = (Element) x;
                        if (XmlUtils.isNode(e, "build", "plugins", "plugin", "configuration", "archive", "manifest", "mainClass")) {
                            //              configuration   execution       executions      plugin
                            Node plugin = e.getParentNode().getParentNode().getParentNode().getParentNode();
                            NId pluginId = parseMavenPluginElement(plugin, session);
                            if (
                                    pluginId.getShortName().equals("org.apache.maven.plugins:maven-assembly-plugin")
                                            || pluginId.getShortName().equals("org.apache.maven.plugins:maven-jar-plugin")
                            ) {
                                String s = NStringUtils.trim(e.getTextContent());
                                if (s.length() > 0) {
                                    s = resolveMainClassString(s, pom);
                                    classes.add(new DefaultNExecutionEntry(s, true, false));
                                }
                            }
                        } else if (XmlUtils.isNode(e, "build", "plugins", "plugin", "executions", "execution", "configuration", "mainClass")) {
                            //              configuration   execution       executions      plugin
                            Node plugin = e.getParentNode().getParentNode().getParentNode().getParentNode();
                            NId pluginId = parseMavenPluginElement(plugin, session);
                            if (
                                    pluginId.getArtifactId().equals("onejar-maven-plugin")
                                            || pluginId.getShortName().equals("org.springframework.boot:spring-boot-maven-plugin")
                                            || pluginId.getShortName().equals("org.openjfx:javafx-maven-plugin")
                            ) {
                                String s = NStringUtils.trim(e.getTextContent());
                                if (s.length() > 0) {
                                    s = resolveMainClassString(s, pom);
                                    classes.add(new DefaultNExecutionEntry(s, true, false));
                                }
                            } else {
                                //what else?
                            }
                        } else if (XmlUtils.isNode(e, "build", "plugins", "plugin", "configuration", "mainClass")) {
                            //              configuration   execution       executions      plugin
                            Node plugin = e.getParentNode().getParentNode();
                            NId pluginId = parseMavenPluginElement(plugin, session);
                            if (pluginId.getShortName().equals("org.springframework.boot:spring-boot-maven-plugin")) {
                                String s = NStringUtils.trim(e.getTextContent());
                                if (s.length() > 0) {
                                    s = resolveMainClassString(s, pom);
                                    classes.add(new DefaultNExecutionEntry(s, true, false));
                                }
                            }
                        } else if (XmlUtils.isNode(e, "build", "plugins", "plugin", "executions", "execution", "configuration", "transformers", "transformer", "mainClass")) {
                            //              configuration   execution       executions      plugin
                            Node plugin = e.getParentNode().getParentNode().getParentNode().getParentNode().getParentNode().getParentNode();
                            NId pluginId = parseMavenPluginElement(plugin, session);
                            if (
                                    pluginId.getShortName().equals("org.apache.maven.plugins:maven-shade-plugin")
                            ) {
                                String s = NStringUtils.trim(e.getTextContent());
                                if (s.length() > 0) {
                                    s = resolveMainClassString(s, pom);
                                    classes.add(new DefaultNExecutionEntry(s, true, false));
                                }
                            }
                        }
                        return true;
                    }
                    return true;
                });
            } else if (path.startsWith("META-INF/nuts/") && path.endsWith("/nuts.json") || path.equals("META-INF/" + NConstants.Files.DESCRIPTOR_FILE_NAME)) {
                NDescriptor descriptor = NDescriptorParser.of(session).parse(inputStream).get(session);
                NArtifactCall executor = descriptor.getExecutor();
                if (executor != null) {
                    List<String> arguments = executor.getArguments();
                    for (int i = 0; i < arguments.size(); i++) {
                        String a = arguments.get(i);
                        if (a != null) {
                            if (a.startsWith("--main-class=")) {
                                classes.add(new DefaultNExecutionEntry(a.substring("--main-class=".length()), true, false));
                            } else if (a.equals("--main-class")) {
                                if (i + 1 < arguments.size()) {
                                    i++;
                                    classes.add(new DefaultNExecutionEntry(a, true, false));
                                }
                            }
                        }
                    }
                }
                NDescriptorProperty mc = descriptor.getProperty("nuts.mainClass").orNull();
                if (mc != null) {
                    String s = NStringUtils.trim(mc.getValue().asString().get(session));
                    if (s.length() > 0) {
                        s = resolveMainClassString(s, descriptor);
                        classes.add(new DefaultNExecutionEntry(s, true, false));
                    }
                }
            }
            return NVisitResult.CONTINUE;
        }, session);

        Map<String, NExecutionEntry> found = new LinkedHashMap<>();
        for (NExecutionEntry entry : classes) {
            String cn = entry.getName();
            NExecutionEntry a = found.get(cn);
            if (a == null) {
                found.put(cn, entry);
            } else {
                if (a.equals(entry)) {
                    //ignore
                } else {
                    NExecutionEntry e2 = new DefaultNExecutionEntry(
                            cn, entry.isDefaultEntry() || a.isDefaultEntry(),
                            entry.isApp() || a.isApp()
                    );
                    found.put(cn, e2);
                }
            }
        }
        List<NExecutionEntry> ee = new ArrayList<>(found.values());
        ee.sort(new Comparator<NExecutionEntry>() {
            @Override
            public int compare(NExecutionEntry o1, NExecutionEntry o2) {
                int x = (o1.isDefaultEntry() ? 0 : 1) - (o2.isDefaultEntry() ? 0 : 1);
                if (x != 0) {
                    return x;
                }
                x = (o1.isApp() ? 0 : 1) - (o2.isApp() ? 0 : 1);
                if (x != 0) {
                    return x;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        return ee;
    }

    private static String resolveMainClassString(String nameOrVar, NPom pom) {
        if (nameOrVar.startsWith("${") && nameOrVar.endsWith("}")) {
            String e = pom.getProperties().get(nameOrVar.substring(2, nameOrVar.length() - 1));
            if (e != null) {
                return e;
            }
        }
        return nameOrVar;
    }

    private static String resolveMainClassString(String nameOrVar, NDescriptor pom) {
        if (nameOrVar.startsWith("${") && nameOrVar.endsWith("}")) {
            String e = pom.getPropertyValue(nameOrVar.substring(2, nameOrVar.length() - 1)).flatMap(NLiteral::asString).orNull();
            if (e != null) {
                return e;
            }
        }
        return nameOrVar;
    }

    public static NId parseMavenPluginElement(Node plugin, NSession session) {
        NIdBuilder ib = NIdBuilder.of();
        for (Node node : XmlUtils.iterable(plugin)) {
            Element ne = XmlUtils.asElement(node);
            if (ne != null) {
                switch (ne.getNodeName()) {
                    case "groupId": {
                        ib.setGroupId(NStringUtils.trim(ne.getTextContent()));
                        break;
                    }
                    case "artifactId": {
                        ib.setArtifactId(NStringUtils.trim(ne.getTextContent()));
                        break;
                    }
                    case "version": {
                        ib.setVersion(NStringUtils.trim(ne.getTextContent()));
                        break;
                    }
                }
            }
        }
        return ib.build();
    }

    public static NVersion parseJarClassVersion(NPath path, NSession session) {
        try (InputStream is = path.getInputStream()) {
            return parseJarClassVersion(is, session);
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
    }

    public static String parseDefaultModuleName(NPath jarStream, NSession session) {
        try (InputStream is = jarStream.getInputStream()) {
            return parseDefaultModuleName(is, session);
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
    }

    public static String parseDefaultModuleName(InputStream jarStream, NSession session) {
        NRef<String> automaticModuleName = new NRef<>();
        ZipUtils.visitZipStream(jarStream, (path, inputStream) -> {
            if ("META-INF/MANIFEST.MF".equals(path)) {
                try {
                    Manifest manifest = new Manifest(inputStream);
                    Attributes attrs = manifest.getMainAttributes();
                    for (Object o : attrs.keySet()) {
                        Attributes.Name attrName = (Attributes.Name) o;
                        if ("Automatic-Module-Name".equals(attrName.toString())) {
                            automaticModuleName.setNonNull(NStringUtils.trimToNull(attrs.getValue(attrName)));
                            return NVisitResult.TERMINATE;
                        }
                    }
                } finally {
                    inputStream.close();
                }
                return NVisitResult.TERMINATE;
            }
            return NVisitResult.CONTINUE;
        }, session);
        return automaticModuleName.get();
    }

    public static JavaClassByteCode.ModuleInfo parseModuleInfo(NPath jar, NSession session) {
        try (InputStream is = jar.getInputStream()) {
            return parseModuleInfo(is, session);
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
    }

    public static JavaClassByteCode.ModuleInfo parseModuleInfo(InputStream jarStream, NSession session) {
        if (!(jarStream instanceof BufferedInputStream)) {
            jarStream = new BufferedInputStream(jarStream);
        }
        NRef<JavaClassByteCode.ModuleInfo> ref = NRef.ofNull();
        ZipUtils.visitZipStream(jarStream, (path, inputStream) -> {
            if (path.equals("module-info.class")) {
                JavaClassByteCode s = new JavaClassByteCode(inputStream, new JavaClassByteCode.Visitor() {
                    @Override
                    public NVisitResult visitClassAttributeModule(JavaClassByteCode.ModuleInfo mi) {
                        ref.set(mi);
                        return NVisitResult.CONTINUE;
                    }
                }, session);
                return NVisitResult.TERMINATE;
            }
            return NVisitResult.CONTINUE;
        }, session);
        return ref.get();
    }
}
