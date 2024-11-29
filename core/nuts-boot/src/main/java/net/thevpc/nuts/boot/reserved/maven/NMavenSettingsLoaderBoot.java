package net.thevpc.nuts.boot.reserved.maven;

import net.thevpc.nuts.boot.NBootRepositoryLocation;
import net.thevpc.nuts.boot.reserved.util.NBootLog;
import net.thevpc.nuts.boot.reserved.util.NBootMsg;
import net.thevpc.nuts.boot.reserved.util.NBootIOUtilsBoot;
import net.thevpc.nuts.boot.reserved.util.NBootStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;

public class NMavenSettingsLoaderBoot {
    private NBootLog log;
    private String settingsFilePath;

    public NMavenSettingsLoaderBoot(NBootLog log) {
        this.log = log;
    }

    public NBootLog getLog() {
        return log;
    }

    public NMavenSettingsLoaderBoot setLog(NBootLog log) {
        this.log = log;
        return this;
    }

    public String getSettingsFilePath() {
        return settingsFilePath;
    }

    public NMavenSettingsLoaderBoot setSettingsFilePath(String settingsFilePath) {
        this.settingsFilePath = settingsFilePath;
        return this;
    }

    private static Boolean elementBoolean(Node c, boolean def) {
        String t = elementText(c);
        if (t.isEmpty()) {
            return def;
        }
        return Boolean.parseBoolean(t);
    }

    private static String elementText(Node c) {
        String e = c == null ? null : c.getTextContent();
        if (e == null) {
            e = "";
        }
        e = e.trim();
        return e;
    }

    private static List<Element> elementsByName(Node c, String name) {
        return elements(c, x -> nodeHasName(c, name));
    }

    private static List<Element> elements(Node c) {
        return elements(c, null);
    }

    private static Element element(Node c, Predicate<Element> cond) {
        List<Element> all = elements(c, cond);
        if(all.isEmpty()){
            return null;
        }
        return all.get(0);
    }

    private static List<Element> elements(Node c, Predicate<Element> cond) {
        return (List) nodes(c, x -> x instanceof Element && (cond == null || cond.test((Element) x)));
    }

    private static List<Node> nodes(Node c, Predicate<Node> cond) {
        List<Node> li = new ArrayList<>();
        NodeList a = c.getChildNodes();
        for (int i = 0; i < a.getLength(); i++) {
            Node e = a.item(i);
            if (cond == null || cond.test(e)) {
                li.add(e);
            }
        }
        return li;
    }

    private static boolean nodeHasName(Node e, String name) {
        return Objects.equals(e.getNodeName(), name);
    }


    public NMavenSettingsBoot loadSettingsRepos() {
        String settingsFilePath = this.settingsFilePath;
        ArrayList<NBootRepositoryLocation> list = new ArrayList<>();
        NMavenSettingsBoot settings = new NMavenSettingsBoot();
        if (NBootStringUtils.isBlank(settingsFilePath)) {
            settingsFilePath = System.getProperty("user.home") + NBootIOUtilsBoot.getNativePath("/.m2/settings.xml");
        }
        Path path = Paths.get(settingsFilePath);
        if (Files.isRegularFile(path) && Files.isReadable(path)) {
            try (InputStream xml = Files.newInputStream(path)) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = null;
                doc = builder.parse(xml);
                Element c = doc.getDocumentElement();
                for (Element e : elements(c)) {
                    switch (e.getNodeName()) {
                        case "localRepository": {
                            String url0 = elementText(e);
                            if (!NBootStringUtils.isBlank(url0)) {
                                settings.setLocalRepository(url0.trim());
                            }
                        }
                        case "mirrors": {
                            for (Element mirror : elements(e, x -> x.getNodeName().equals("mirror"))) {
                                String id = elementText((element(mirror, x -> x.getNodeName().equals("id"))));
                                String url0 = elementText((element(mirror, x -> x.getNodeName().equals("url"))));
                                if (!NBootStringUtils.isBlank(id) && !NBootStringUtils.isBlank(url0)) {
                                    list.add(new NBootRepositoryLocation(id.trim(), "maven", url0.trim()));
                                }
                            }
                            break;
                        }
                        case "profiles": {
                            for (Element profile : elements(e, x -> x.getNodeName().equals("profile"))) {
                                boolean active = true;
                                for (Element activation : elements(profile, x -> x.getNodeName().equals("activation"))) {
                                    for (Element activeByDefault : elements(activation, x -> x.getNodeName().equals("activeByDefault"))) {
                                        active = elementBoolean(activeByDefault, active);
                                    }
                                }
                                if (active) {
                                    for (Element repositories : elements(profile, x -> x.getNodeName().equals("repositories"))) {
                                        for (Element repository : elements(repositories, x -> x.getNodeName().equals("repository"))) {
                                            String id = elementText((element(repository, x -> x.getNodeName().equals("id"))));
                                            String url0 = elementText((element(repository, x -> x.getNodeName().equals("url"))));
                                            boolean enabled0 = true;
                                            for (Element releases : elements(profile, x -> x.getNodeName().equals("releases"))) {
                                                for (Element enabled : elements(releases, x -> x.getNodeName().equals("enabled"))) {
                                                    enabled0 = elementBoolean(enabled, enabled0);
                                                }
                                            }
                                            if (enabled0 && !NBootStringUtils.isBlank(id) && !NBootStringUtils.isBlank(url0)) {
                                                list.add(new NBootRepositoryLocation(id.trim(), "maven", url0.trim()));
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                log.with().level(Level.FINE).verbFail().error(ex).log(NBootMsg.ofC("unable to load maven settings.xml %s", settingsFilePath));
            }
        }
        if (NBootStringUtils.isBlank(settings.getLocalRepository())) {
            settings.setLocalRepository(System.getProperty("user.home") + NBootIOUtilsBoot.getNativePath("/.m2/repository"));
        }
        if (NBootStringUtils.isBlank(settings.getRemoteRepository())) {
            //always!
            settings.setRemoteRepository("https://repo.maven.apache.org/maven2");
        }
        settings.setActiveRepositories(list);
        return settings;
    }
}
