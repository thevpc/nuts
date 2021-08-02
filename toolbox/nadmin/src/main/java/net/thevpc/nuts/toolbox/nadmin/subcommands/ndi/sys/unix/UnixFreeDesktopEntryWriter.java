package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.sys.unix;

import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.FreeDesktopEntry;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.FreeDesktopEntryWriter;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptInfo;
import net.thevpc.nuts.toolbox.nadmin.util._IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class UnixFreeDesktopEntryWriter implements FreeDesktopEntryWriter {
    private NutsSession session;
    private Path desktopPath;

    public UnixFreeDesktopEntryWriter(NutsSession session,Path desktopPath) {
        this.session = session;
        this.desktopPath = desktopPath;
    }

    @Override
    public PathInfo[] writeDesktop(FreeDesktopEntry file, boolean doOverride) {
        String name = file.getOrCreateDesktopEntry().getName();
        File m = new File(desktopPath.toString());
        File q = new File(m, name + ".desktop");
        boolean alreadyExists = q.exists();
        if (alreadyExists && !doOverride) {
            return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_SHORTCUT, q.toPath(), PathInfo.Status.DISCARDED)};
        }
        write(file, q);
        return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_SHORTCUT, q.toPath(), alreadyExists ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED)};
    }

    @Override
    public PathInfo[] writeMenu(FreeDesktopEntry file, String menuPath, boolean doOverride) {
        String name = file.getOrCreateDesktopEntry().getName();
        String[] part = Arrays.stream((menuPath == null ? "" : menuPath).split("/")).filter(x -> !x.isEmpty()).toArray(String[]::new);
        if (part.length == 0) {
            part = new String[]{"Applications"};
        } else if (!part[0].equals("Applications")) {
            List<String> li = new ArrayList<>();
            li.add("Applications");
            li.addAll(Arrays.asList(part));
            part = li.toArray(new String[0]);
        }
        String menuName = name;//"nuts-" + UUID.randomUUID();
        File folder4shortcuts = new File(System.getProperty("user.home") + "/.local/share/applications");
        File folder4menus = new File(System.getProperty("user.home") + "/.config/menus/applications-merged");
        folder4shortcuts.mkdirs();
        folder4menus.mkdirs();
        try {
            File shortcutFile = new File(folder4shortcuts, name + ".desktop");
            boolean shortcutFileAlreadyExists = shortcutFile.exists();
            byte[] oldShortcutContent = _IOUtils.loadFileContentLenient(shortcutFile.toPath());
            if (!shortcutFileAlreadyExists || doOverride) {
                write(file, shortcutFile);
            }
            PathInfo shortcutPathInfo = new PathInfo(
                    NdiScriptInfo.Type.DESKTOP_MENU,
                    shortcutFile.toPath(), shortcutFileAlreadyExists ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED);

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
            dom.appendChild(createMenuXmlElement(part, menuName + ".desktop", dom));
            // write DOM to XML file
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            tr.transform(new DOMSource(dom), new StreamResult(b));
            File menuFile = new File(folder4menus, name + ".menu");
            byte[] oldMenuContent = _IOUtils.loadFileContentLenient(menuFile.toPath());
            PathInfo menuPathInfo;
            if (Arrays.equals(oldMenuContent, b.toByteArray())) {
                menuPathInfo = new PathInfo(NdiScriptInfo.Type.DESKTOP_MENU, menuFile.toPath(), PathInfo.Status.DISCARDED);
            } else {
                boolean alreadyExists0 = menuFile.isFile();
                if (!alreadyExists0 || doOverride) {
                    Files.write(menuFile.toPath(), b.toByteArray());
                }
                if (alreadyExists0) {
                    menuPathInfo = new PathInfo(NdiScriptInfo.Type.DESKTOP_MENU,menuFile.toPath(), PathInfo.Status.OVERRIDDEN);
                } else {
                    menuPathInfo = new PathInfo(NdiScriptInfo.Type.DESKTOP_MENU,menuFile.toPath(), PathInfo.Status.CREATED);
                }
            }

            return new PathInfo[]{shortcutPathInfo, menuPathInfo};
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (ParserConfigurationException | TransformerException ex) {
            throw new RuntimeException(ex);
        }
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

    public boolean tryWrite(FreeDesktopEntry file, Path out) {
        String old = null;
        if (Files.isRegularFile(out)) {
            try {
                old = new String(Files.readAllBytes(out));
            } catch (Exception ex) {
                //ignore
            }
        }
        if (old == null) {
            write(file, out);
            return true;
        }
        String s = writeAsString(file);
        if (old.trim().equals(s.trim())) {
            return false;
        }
        try {
            Files.write(out, s.getBytes());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return true;
    }

    public boolean tryWrite(FreeDesktopEntry file, File out) {
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
                    v=Character.toUpperCase(v.charAt(0))+v.substring(1);
                    out.println(key + "=" + v);
                }else if (value instanceof Boolean || value instanceof String) {
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
