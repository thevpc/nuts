package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamVisitor;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.Pom;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.PomXmlParser;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import net.thevpc.nuts.runtime.standalone.xtra.execentries.DefaultNutsExecutionEntry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JavaJarUtils {
    public static void main(String[] args) {
        String spath="/data/git/nuts/core/nuts/target/nuts-0.8.3.jar";
        final Map<String,Set<String>> classes = new HashMap<>();
        try(InputStream jarStream= Files.newInputStream(Paths.get(spath))) {
            ZipUtils.visitZipStream(jarStream, (path, inputStream) -> {
                if (path.endsWith(".class")) {
                    JavaClassByteCode.Visitor cl = new JavaClassByteCode.Visitor() {
                        @Override
                        public boolean visitVersion(int major, int minor) {
                            String v = JavaClassUtils.classVersionToSourceVersion(major, minor, null);
                            Set<String> r = classes.get(v);
                            if(r==null){
                                r=new HashSet<>();
                                classes.put(v,r);
                            }
                            r.add(path.substring(0,path.length()-6));
                            return false;
                        }
                    };
                    JavaClassByteCode classReader = new JavaClassByteCode(new BufferedInputStream(inputStream), cl, null);

                }
                return true;
            }, null);
        }catch (IOException ex){
            ex.printStackTrace();
        }
        for (Map.Entry<String, Set<String>> e : classes.entrySet()) {
            System.out.println(e.getKey()+" ("+e.getValue().size()+")");
        }
        System.out.println("DETAILS:");
        for (Map.Entry<String, Set<String>> e : classes.entrySet()) {
            System.out.println(e.getKey()+" ("+e.getValue().size()+")");
            for (String s : e.getValue()) {
                System.out.println("\t"+s);
            }
        }
    }
    public static NutsVersion[] parseJarClassVersions(InputStream jarStream, NutsSession session) {
        if (!(jarStream instanceof BufferedInputStream)) {
            jarStream = new BufferedInputStream(jarStream);
        }
        final HashSet<String> classes = new HashSet<>();
        ZipUtils.visitZipStream(jarStream, (path, inputStream) -> {
            if(path.endsWith(".class")) {
                JavaClassByteCode.Visitor cl = new JavaClassByteCode.Visitor() {
                    @Override
                    public boolean visitVersion(int major, int minor) {
                        classes.add(JavaClassUtils.classVersionToSourceVersion(major, minor, session));
                        return false;
                    }

                };
                JavaClassByteCode classReader = new JavaClassByteCode(new BufferedInputStream(inputStream), cl, session);
            }
            return true;
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

    public static NutsExecutionEntry[] parseJarExecutionEntries(InputStream jarStream, NutsSession session) {
        if (!(jarStream instanceof BufferedInputStream)) {
            jarStream = new BufferedInputStream(jarStream);
        }
        final LinkedHashSet<NutsExecutionEntry> classes = new LinkedHashSet<>();
        ZipUtils.visitZipStream(jarStream, (path, inputStream) -> {
            if (path.endsWith(".class")) {
                NutsExecutionEntry mainClass = JavaClassUtils.parseClassExecutionEntry(inputStream, path, session);
                if (mainClass != null) {
                    classes.add(mainClass);
                }
            } else if (path.equals("META-INF/MANIFEST.MF")) {
                Manifest manifest = new Manifest(inputStream);
                Attributes a = manifest.getMainAttributes();
                if (a != null) {
                    String v = a.getValue("Main-Class");
                    if (!NutsBlankable.isBlank(v)) {
                        v=NutsUtilStrings.trim(v);
                        classes.add(new DefaultNutsExecutionEntry(v, true, false));
                    }
                }
            } else if (path.startsWith("META-INF/maven/") && path.endsWith("/pom.xml")) {
                Pom pom = new PomXmlParser(session).parse(inputStream, session);
                final Element ee = pom.getXml().getDocumentElement();
                if(pom.getParent()!=null && pom.getParent().getArtifactId().equals("spring-boot-starter-parent")){
                    String springStartClass = NutsUtilStrings.trim(pom.getProperties().get("start-class"));
                    if(springStartClass.length()>0){
                        classes.add(new DefaultNutsExecutionEntry(springStartClass, true, false));
                    }
                }
                XmlUtils.visitNode(ee, x -> {
                    if (x instanceof Element) {
                        Element e = (Element) x;
                        if (XmlUtils.isNode(e, "build", "plugins", "plugin", "configuration", "archive", "manifest", "mainClass")) {
                            //              configuration   execution       executions      plugin
                            Node plugin = e.getParentNode().getParentNode().getParentNode().getParentNode();
                            NutsId pluginId = parseMavenPluginElement(plugin, session);
                            if (
                                    pluginId.getShortName().equals("org.apache.maven.plugins:maven-assembly-plugin")
                                            || pluginId.getShortName().equals("org.apache.maven.plugins:maven-jar-plugin")
                            ) {
                                String s = NutsUtilStrings.trim(e.getTextContent());
                                if (s.length() > 0) {
                                    s=resolveMainClassString(s,pom);
                                    classes.add(new DefaultNutsExecutionEntry(s, true, false));
                                }
                            }
                        }else if (XmlUtils.isNode(e, "build", "plugins", "plugin", "executions", "execution", "configuration", "mainClass")) {
                            //              configuration   execution       executions      plugin
                            Node plugin = e.getParentNode().getParentNode().getParentNode().getParentNode();
                            NutsId pluginId = parseMavenPluginElement(plugin, session);
                            if (
                                       pluginId.getArtifactId().equals("onejar-maven-plugin")
                                    || pluginId.getShortName().equals("org.springframework.boot:spring-boot-maven-plugin")
                                    || pluginId.getShortName().equals("org.openjfx:javafx-maven-plugin")
                            ) {
                                String s = NutsUtilStrings.trim(e.getTextContent());
                                if (s.length() > 0) {
                                    s=resolveMainClassString(s,pom);
                                    classes.add(new DefaultNutsExecutionEntry(s, true, false));
                                }
                            }else{
                                //what else?
                            }
                        }else if (XmlUtils.isNode(e, "build", "plugins", "plugin", "configuration", "mainClass")) {
                            //              configuration   execution       executions      plugin
                            Node plugin = e.getParentNode().getParentNode();
                            NutsId pluginId = parseMavenPluginElement(plugin, session);
                            if (pluginId.getShortName().equals("org.springframework.boot:spring-boot-maven-plugin")) {
                                String s = NutsUtilStrings.trim(e.getTextContent());
                                if (s.length() > 0) {
                                    s=resolveMainClassString(s,pom);
                                    classes.add(new DefaultNutsExecutionEntry(s, true, false));
                                }
                            }
                        }else if (XmlUtils.isNode(e, "build", "plugins", "plugin","executions","execution", "configuration", "transformers", "transformer", "mainClass")) {
                            //              configuration   execution       executions      plugin
                            Node plugin = e.getParentNode().getParentNode().getParentNode().getParentNode().getParentNode().getParentNode();
                            NutsId pluginId = parseMavenPluginElement(plugin, session);
                            if (
                                    pluginId.getShortName().equals("org.apache.maven.plugins:maven-shade-plugin")
                            ) {
                                String s = NutsUtilStrings.trim(e.getTextContent());
                                if (s.length() > 0) {
                                    s=resolveMainClassString(s,pom);
                                    classes.add(new DefaultNutsExecutionEntry(s, true, false));
                                }
                            }
                        }
                        return true;
                    }
                    return true;
                });
            } else if (path.startsWith("META-INF/nuts/") && path.endsWith("/nuts.json") || path.equals("META-INF/"+NutsConstants.Files.DESCRIPTOR_FILE_NAME)) {
                NutsDescriptor descriptor = NutsDescriptorParser.of(session).parse(inputStream);
                NutsArtifactCall executor = descriptor.getExecutor();
                if(executor!=null){
                    String[] arguments = executor.getArguments();
                    for (int i = 0; i < arguments.length; i++) {
                        String argument = arguments[i];
                        if (argument != null) {
                            if (argument.startsWith("--main-class=")) {
                                classes.add(new DefaultNutsExecutionEntry(argument.substring("--main-class=".length()), true, false));
                            } else if (argument.equals("--main-class")) {
                                if(i+1<arguments.length){
                                    i++;
                                    classes.add(new DefaultNutsExecutionEntry(arguments[i], true, false));
                                }
                            }
                        }
                    }
                }
                NutsDescriptorProperty mc = descriptor.getProperty("nuts.mainClass");
                if(mc!=null){
                    String s = NutsUtilStrings.trim(mc.getValue());
                    if (s.length() > 0) {
                        s=resolveMainClassString(s,descriptor);
                        classes.add(new DefaultNutsExecutionEntry(s, true, false));
                    }
                }
            }
            return true;
        }, session);

        Map<String,NutsExecutionEntry> found = new LinkedHashMap<>();
        for (NutsExecutionEntry entry : classes) {
            String cn = entry.getName();
            NutsExecutionEntry a = found.get(cn);
            if(a==null){
                found.put(cn,entry);
            }else{
                if(a.equals(entry)){
                    //ignore
                }else{
                    NutsExecutionEntry e2=new DefaultNutsExecutionEntry(
                            cn,entry.isDefaultEntry()||a.isDefaultEntry(),
                            entry.isApp()||a.isApp()
                    );
                    found.put(cn,e2);
                }
            }
        }
        NutsExecutionEntry[] ee = found.values().toArray(new NutsExecutionEntry[0]);
        Arrays.sort(ee, new Comparator<NutsExecutionEntry>() {
            @Override
            public int compare(NutsExecutionEntry o1, NutsExecutionEntry o2) {
                int x=(o1.isDefaultEntry()?0:1)-(o2.isDefaultEntry()?0:1);
                if(x!=0){
                    return x;
                }
                x=(o1.isApp()?0:1)-(o2.isApp()?0:1);
                if(x!=0){
                    return x;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        return ee;
    }

    private static String resolveMainClassString(String nameOrVar, Pom pom) {
        if(nameOrVar.startsWith("${") && nameOrVar.endsWith("}")){
            String e = pom.getProperties().get(nameOrVar.substring(2, nameOrVar.length() - 1));
            if(e!=null){
                return e;
            }
        }
        return nameOrVar;
    }
    private static String resolveMainClassString(String nameOrVar, NutsDescriptor pom) {
        if(nameOrVar.startsWith("${") && nameOrVar.endsWith("}")){
            String e = pom.getPropertyValue(nameOrVar.substring(2, nameOrVar.length() - 1));
            if(e!=null){
                return e;
            }
        }
        return nameOrVar;
    }

    public static NutsId parseMavenPluginElement(Node plugin, NutsSession session) {
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
                    case "version": {
                        ib.setVersion(NutsUtilStrings.trim(ne.getTextContent()));
                        break;
                    }
                }
            }
        }
        return ib.build();
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
        ZipUtils.visitZipStream(jarStream, (path, inputStream) -> {
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
                return false;
            }
            return true;
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
        NutsRef<JavaClassByteCode.ModuleInfo> ref = new NutsRef<>();
        ZipUtils.visitZipStream(jarStream, (path, inputStream) -> {
            if(path.equals("module-info.class")) {
                JavaClassByteCode s = new JavaClassByteCode(inputStream, new JavaClassByteCode.Visitor() {
                    @Override
                    public boolean visitClassAttributeModule(JavaClassByteCode.ModuleInfo mi) {
                        ref.set(mi);
                        return true;
                    }
                }, session);
                return false;
            }
            return true;
        }, session);
        return ref.get();
    }
}
