package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.sys.unix;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.PathInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.AbstractFreeDesktopEntryWriter;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.FreeDesktopEntry;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.NdiScriptInfoType;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.util.NdiUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UnixFreeDesktopEntryWriter extends AbstractFreeDesktopEntryWriter {
    private NutsSession session;
    private Path desktopPath;

    public UnixFreeDesktopEntryWriter(NutsSession session, Path desktopPath) {
        this.session = session;
        this.desktopPath = desktopPath;
    }


    @Override
    public PathInfo[] writeShortcut(FreeDesktopEntry descriptor, Path path, boolean doOverride, NutsId id) {
        path = Paths.get(ensureName(path == null ? null : path.toString(), descriptor.getOrCreateDesktopEntry().getName(), "desktop"));
        PathInfo.Status s = tryWrite(descriptor, path);
        return new PathInfo[]{new PathInfo(NdiScriptInfoType.DESKTOP_SHORTCUT, id, path, s)};
    }

    @Override
    public PathInfo[] writeDesktop(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NutsId id) {
        fileName = Paths.get(ensureName(fileName, descriptor.getOrCreateDesktopEntry().getName(), "desktop")).getFileName().toString();
        File q = desktopPath.resolve(fileName).toFile();
        return writeShortcut(descriptor, q.toPath(), doOverride, id);
    }

    @Override
    public PathInfo[] writeMenu(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NutsId id) {
        String menuFileName = Paths.get(ensureName(fileName, descriptor.getOrCreateDesktopEntry().getName(), "menu")).getFileName().toString();
        String desktopFileName = Paths.get(ensureName(fileName, descriptor.getOrCreateDesktopEntry().getName(), "desktop")).getFileName().toString();

        List<PathInfo> all = new ArrayList<>();
        FreeDesktopEntry.Group root = descriptor.getOrCreateDesktopEntry();
        File folder4shortcuts = new File(System.getProperty("user.home") + "/.local/share/applications");
        folder4shortcuts.mkdirs();
        File shortcutFile = new File(folder4shortcuts, desktopFileName);
        all.add(new PathInfo(NdiScriptInfoType.DESKTOP_MENU, id,
                shortcutFile.toPath(), tryWrite(descriptor, shortcutFile)));

        List<String> categories = new ArrayList<>(root.getCategories());
        if (categories.isEmpty()) {
            categories.add("/");
        }
        File folder4menus = new File(System.getProperty("user.home") + "/.config/menus/applications-merged");
        folder4menus.mkdirs();

        for (String menuPath : categories) {
            String[] part = Arrays.stream((menuPath == null ? "" : menuPath).split("/")).filter(x -> !x.isEmpty()).toArray(String[]::new);
            if (part.length == 0) {
                part = new String[]{"Applications"};
            } else if (!part[0].equals("Applications")) {
                List<String> li = new ArrayList<>();
                li.add("Applications");
                li.addAll(Arrays.asList(part));
                part = li.toArray(new String[0]);
            }
            try {

                //menu
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document dom = builder.newDocument();
//        <Menu>
//    <Name>Applications</Name>
//    <Menu>
//        <Directory>YourApp-top.directory</Directory>
//        <Name>YourApp-top</Name>
//        <Menu>
//            <Directory>YourApp-second.directory</Directory>
//            <Name>YourApp-second</Name>
//            <Include>
//                <Filename>YourApp-test.desktop</Filename>
//            </Include>
//        </Menu>
//    </Menu>
//</Menu>
                dom.appendChild(createMenuXmlElement(part, desktopFileName, dom));
                // write DOM to XML file
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                tr.transform(new DOMSource(dom), new StreamResult(b));
                File menuFile = new File(folder4menus, menuFileName);
                all.add(new PathInfo(NdiScriptInfoType.DESKTOP_MENU, id, menuFile.toPath(), NdiUtils.tryWrite(b.toByteArray(), menuFile.toPath())));
            } catch (ParserConfigurationException | TransformerException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (all.stream().anyMatch(x -> x.getStatus() != PathInfo.Status.DISCARDED)) {
            updateDesktopMenus();
        }
        return all.toArray(new PathInfo[0]);
    }

    private void updateDesktopMenus() {
        //    KDE  : 'kbuildsycoca5'
        Path a = NdiUtils.sysWhich("kbuildsycoca5");
        if (a != null) {
            String outStr = session.getWorkspace().exec().setCommand(a.toString()).userCmd()
                    .setRedirectErrorStream(true).grabOutputString()
                    .run()
                    .getOutputString();
            if(session.isPlainTrace()){
                session.out().println(outStr);
            }
        }

        //    GNOME: update-desktop-database ~/.local/share/applications
        a = NdiUtils.sysWhich("update-desktop-database");
        if (a != null) {
            String outStr = session.getWorkspace().exec().setCommand(a.toString(), System.getProperty("user.home") + "/.local/share/applications").userCmd()
                    .setRedirectErrorStream(true).grabOutputString()
                    .run()
                    .getOutputString();
            if(session.isPlainTrace()){
                session.out().println(outStr);
            }
        }

        // more generic : xdg-desktop-menu forceupdate
        a = NdiUtils.sysWhich("xdg-desktop-menu");
        if (a != null) {
            String outStr = session.getWorkspace().exec().setCommand(a.toString(), "forceupdate").userCmd()
                    .setRedirectErrorStream(true).grabOutputString()
                    .run()
                    .getOutputString();
            if(session.isPlainTrace()){
                session.out().println(outStr);
            }
        }
    }

    private boolean isKDE() {
        return "kde".equalsIgnoreCase(getDesktopEnvironment());
    }

    private boolean isGNOME() {
        return "gnome".equalsIgnoreCase(getDesktopEnvironment());
    }

    private String getDesktopEnvironment() {
        return System.getenv("XDG_SESSION_DESKTOP");
    }

    private Element createMenuXmlElement(String[] a, String name, Document dom) {
        if (a.length == 1) {
            Element menu = dom.createElement("Menu");
            Element d = dom.createElement("Name");
            d.setTextContent(a[0]);
            Element f = dom.createElement("Filename");
            f.setTextContent(name);
            Element i = dom.createElement("Include");
            i.appendChild(f);
            menu.appendChild(d);
            menu.appendChild(i);
            return menu;
        } else {
            Element menu = dom.createElement("Menu");
            Element d = dom.createElement("Name");
            d.setTextContent(a[0]);
            menu.appendChild(d);
            menu.appendChild(createMenuXmlElement(Arrays.copyOfRange(a, 1, a.length), name, dom));
            return menu;
        }
    }

    public void write(FreeDesktopEntry file, Path out) {
        try (PrintStream p = new PrintStream(Files.newOutputStream(out))) {
            write(file, p);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }


    public PathInfo.Status tryWrite(FreeDesktopEntry file, Path out) {
        return NdiUtils.tryWrite(writeAsString(file).getBytes(), out);
    }

    public PathInfo.Status tryWrite(FreeDesktopEntry file, File out) {
        return tryWrite(file, out.toPath());
    }

    public String writeAsString(FreeDesktopEntry file) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bos);
        write(file, out);
        out.flush();
        return bos.toString();
    }


    public void write(FreeDesktopEntry file, File out) {
        try (PrintStream p = new PrintStream(out)) {
            write(file, p);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void write(FreeDesktopEntry file, NutsPrintStream out) {
        write(file, out.asPrintStream());
    }

    public void write(FreeDesktopEntry file, PrintStream out) {
        out.println("#!/usr/bin/env xdg-open");
        for (FreeDesktopEntry.Group group : file.getGroups()) {
            out.println();
            String gn = group.getGroupName();
            if (gn == null || gn.trim().length() == 0) {
                throw new IllegalArgumentException("invalid group name");
            }
            FreeDesktopEntry.Type t = group.getType();
            if (t == null) {
                throw new IllegalArgumentException("missing type");
            }
            out.println("[" + gn.trim() + "]");
            for (Map.Entry<String, Object> e : group.toMap().entrySet()) {
                Object value = e.getValue();
                String key = e.getKey();
                if (value instanceof FreeDesktopEntry.Type) {
                    String v = value.toString().toLowerCase();
                    v = Character.toUpperCase(v.charAt(0)) + v.substring(1);
                    out.println(key + "=" + v);
                } else if (value instanceof Boolean || value instanceof String) {
                    out.println(key + "=" + value);
                } else if (value instanceof List) {
                    char sep = ';';
                    out.println(key + "=" +
                            ((List<String>) value).stream().map(x -> {
                                StringBuilder sb = new StringBuilder();
                                for (char c : x.toCharArray()) {
                                    if (c == sep || c == '\\') {
                                        sb.append('\\');
                                    }
                                    sb.append(c);
                                }
                                return sb.toString();
                            }).collect(Collectors.joining("" + sep))
                    );
                } else {
                    throw new IllegalArgumentException("unsupported value type for " + key);
                }
            }

        }
    }
}
