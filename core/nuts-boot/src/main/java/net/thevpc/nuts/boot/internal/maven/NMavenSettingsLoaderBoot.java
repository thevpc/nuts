package net.thevpc.nuts.boot.internal.maven;

import net.thevpc.nuts.boot.NBootRepositoryLocation;
import net.thevpc.nuts.boot.internal.util.*;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;

public class NMavenSettingsLoaderBoot {
    private String settingsFilePath;

    public NMavenSettingsLoaderBoot() {
    }

    public String settingsFilePath() {
        return settingsFilePath;
    }

    public NMavenSettingsLoaderBoot settingsFilePath(String settingsFilePath) {
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

    private static List<Element> elements(Node c) {
        return elements(c, null);
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

    public NMavenSettingsBoot loadSettingsRepos() {
        String settingsFilePath = this.settingsFilePath;
        List<NBootRepositoryLocation> list = new ArrayList<>();
        NMavenSettingsBoot settings = new NMavenSettingsBoot();
        if (NBootUtils.isBlank(settingsFilePath)) {
            settingsFilePath = System.getProperty("user.home") + NBootUtils.getNativePath("/.m2/settings.xml");
        }
        Path path = Paths.get(settingsFilePath);
        if (Files.isRegularFile(path) && Files.isReadable(path)) {
            try (InputStream xml = Files.newInputStream(path)) {
                DocumentBuilder builder = newSecureDocumentBuilder();
                Document doc = builder.parse(xml);
                Element c = doc.getDocumentElement();
                // ids of the profiles explicitly requested through <activeProfiles>
                Set<String> requestedProfiles = new HashSet<>();
                for (Element e : elements(c, x -> x.getNodeName().equals("activeProfiles"))) {
                    for (Element ap : elements(e, x -> x.getNodeName().equals("activeProfile"))) {
                        String id = elementText(ap);
                        if (!id.isEmpty()) {
                            requestedProfiles.add(id);
                        }
                    }
                }
                for (Element e : elements(c)) {
                    switch (e.getNodeName()) {
                        case "localRepository": {
                            String url0 = elementText(e);
                            if (!NBootUtils.isBlank(url0)) {
                                settings.localRepository(NMavenSettingsUtilsBoot.expandPath(url0.trim()));
                            }
                            break;
                        }
                        case "mirrors": {
                            for (Element mirror : elements(e, x -> x.getNodeName().equals("mirror"))) {
                                String id = firstElementText(mirror, "id");
                                String url0 = firstElementText(mirror, "url");
                                String mirrorOf = firstElementText(mirror, "mirrorOf");
                                if (!NBootUtils.isBlank(id) && !NBootUtils.isBlank(url0)) {
                                    // nuts has no notion of mirror replacement, so a mirror is used
                                    // as an additional repository. warn when it claims to replace
                                    // central, since that cannot be honoured.
                                    if (NBootUtils.isBlank(mirrorOf) || NMavenSettingsUtilsBoot.mirrorsCentral(mirrorOf)) {
                                        NBootContext.log().log(Level.WARNING, NBootLog.INTENT_ALERT, NBootMsg.ofC("maven mirror %s (%s) replaces central but nuts uses it as an extra repository", id, url0).asWarning());
                                    }
                                    list.add(NBootRepositoryLocation.of(id.trim(), "maven", url0.trim()));
                                } else {
                                    NBootContext.log().log(Level.WARNING, NBootLog.INTENT_ALERT, NBootMsg.ofC("ignoring incomplete maven mirror declaration in %s", settingsFilePath).asWarning());
                                }
                            }
                            break;
                        }
                        case "profiles": {
                            for (Element profile : elements(e, x -> x.getNodeName().equals("profile"))) {
                                String profileId = firstElementText(profile, "id");
                                if (!isProfileActive(profile, profileId, requestedProfiles)) {
                                    continue;
                                }
                                for (Element repositories : elements(profile, x -> x.getNodeName().equals("repositories"))) {
                                    for (Element repository : elements(repositories, x -> x.getNodeName().equals("repository"))) {
                                        String id = firstElementText(repository, "id");
                                        String url0 = firstElementText(repository, "url");
                                        if (NBootUtils.isBlank(id) || NBootUtils.isBlank(url0)) {
                                            NBootContext.log().log(Level.WARNING, NBootLog.INTENT_ALERT, NBootMsg.ofC("ignoring incomplete maven repository declaration in profile %s", profileId).asWarning());
                                            continue;
                                        }
                                        if (!isRepositoryEnabled(repository)) {
                                            continue;
                                        }
                                        String trimmedId = id.trim();
                                        boolean duplicate = false;
                                        for (NBootRepositoryLocation already : list) {
                                            if (trimmedId.equals(already.name())) {
                                                duplicate = true;
                                                break;
                                            }
                                        }
                                        if (duplicate) {
                                            NBootContext.log().log(Level.WARNING, NBootLog.INTENT_ALERT, NBootMsg.ofC("ignoring duplicate maven repository id %s in profile %s", trimmedId, profileId).asWarning());
                                            continue;
                                        }
                                        list.add(NBootRepositoryLocation.of(trimmedId, "maven", url0.trim()));
                                    }
                                }
                            }
                            break;
                        }
                        default:
                            break;
                    }
                }
            } catch (Exception ex) {
                NBootContext.log().with().level(Level.FINE).verbFail().error(ex).log(NBootMsg.ofC("unable to load maven settings.xml %s", settingsFilePath));
            }
        }
        if (NBootUtils.isBlank(settings.localRepository())) {
            settings.localRepository(System.getProperty("user.home") + NBootUtils.getNativePath("/.m2/repository"));
        }
        if (NBootUtils.isBlank(settings.remoteRepository())) {
            //always!
            settings.remoteRepository("https://repo.maven.apache.org/maven2");
        }
        settings.activeRepositories(list);
        return settings;
    }

    /**
     * Maven activates a profile when it is requested through
     * {@code <activeProfiles>}, or when it declares
     * {@code <activeByDefault>true</activeByDefault>}. Any other activation
     * condition ({@code jdk}, {@code os}, {@code property}, {@code file}) is not
     * evaluated, so such a profile is left inactive rather than being wrongly
     * assumed to apply.
     *
     * @param profile           profile element
     * @param profileId         profile id
     * @param requestedProfiles ids listed in {@code <activeProfiles>}
     * @return true if the profile must be considered
     */
    private boolean isProfileActive(Element profile, String profileId, Set<String> requestedProfiles) {
        if (!profileId.isEmpty() && requestedProfiles.contains(profileId)) {
            return true;
        }
        for (Element activation : elements(profile, x -> x.getNodeName().equals("activation"))) {
            for (Element activeByDefault : elements(activation, x -> x.getNodeName().equals("activeByDefault"))) {
                if (elementBoolean(activeByDefault, false)) {
                    return true;
                }
            }
            for (Element unsupported : elements(activation)) {
                String n = unsupported.getNodeName();
                if (!"activeByDefault".equals(n)) {
                    NBootContext.log().log(Level.WARNING, NBootLog.INTENT_ALERT, NBootMsg.ofC("maven profile %s activation by <%s> is not supported and is ignored", profileId, n).asWarning());
                }
            }
        }
        return false;
    }

    /**
     * Reads {@code <releases><enabled>} from the repository element itself.
     *
     * @param repository repository element
     * @return true if the repository may be used to resolve releases
     */
    private static boolean isRepositoryEnabled(Element repository) {
        for (Element releases : elements(repository, x -> x.getNodeName().equals("releases"))) {
            for (Element enabled : elements(releases, x -> x.getNodeName().equals("enabled"))) {
                if (!elementBoolean(enabled, true)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String firstElementText(Element parent, String name) {
        for (Element e : elements(parent, x -> x.getNodeName().equals(name))) {
            return elementText(e);
        }
        return "";
    }

    /**
     * Creates a {@link DocumentBuilder} with external entity processing and
     * DOCTYPE declarations disabled. {@code settings.xml} lives in a
     * user-writable home directory, so parsing it must not be able to read
     * arbitrary files or reach the network.
     *
     * @return a hardened document builder
     * @throws Exception if the parser cannot be hardened
     */
    private static DocumentBuilder newSecureDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        trySetFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl");
        return factory.newDocumentBuilder();
    }

    private static void trySetFeature(DocumentBuilderFactory factory, String feature) {
        try {
            factory.setFeature(feature, true);
        } catch (Exception ignored) {
            // not supported by this parser, the features above already cover it
        }
    }
}
