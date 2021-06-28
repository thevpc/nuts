/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.docusaurus;


import net.thevpc.nuts.lib.md.MdElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class DocusaurusFile implements DocusaurusFileOrFolder {

    private final Type type;
    private final String longId;
    private final String shortId;
    private final String title;
    private final Path path;
    private final String content;
    private final MdElement tree;
    private final int order;

    public static DocusaurusFile ofContent(String id, String longId, String title, String content, int menuOrder) {
        return new DocusaurusFile(Type.CONTENT, id, longId,title, null, content, null,menuOrder);
    }

    public static DocusaurusFile ofPath(String id, String longId, String title, Path path, int menuOrder) {
        return new DocusaurusFile(Type.PATH, id, longId,title, path, null, null,menuOrder);
    }

    public static DocusaurusFile ofTree(String id, String longId, String title, MdElement tree, int menuOrder) {
        return new DocusaurusFile(Type.TREE, id, longId,title, null, null, tree,menuOrder);
    }

    public static DocusaurusFile ofFile(Path path, Path root) {
        int from = root.getNameCount();
        int to = path.getNameCount() - 1;
        String partialPath = from == to ? "" : path.subpath(from, to).toString();
        try {
            BufferedReader br = Files.newBufferedReader(path);
            String line1 = br.readLine();
            if ("---".equals(line1)) {
                Map<String, String> props = new HashMap<>();
                while (true) {
                    line1 = br.readLine();
                    if (line1 == null || line1.equals("---")) {
                        break;
                    }
                    if (line1.matches("[a-z_]+:.*")) {
                        int colon = line1.indexOf(':');
                        props.put(line1.substring(0, colon).trim(), line1.substring(colon + 1).trim());
                    }
                }
                String id = props.get("id");
                Integer menu_order = DocusaurusUtils.parseInt(props.get("order"));
                if (menu_order != null) {
                    if (menu_order.intValue() <= 0) {
                        throw new IllegalArgumentException("invalid order in " + path);
                    }
                } else {
                    menu_order = 0;
                }
                if (id != null) {
                    return DocusaurusFile.ofPath(id, (partialPath == null || partialPath.isEmpty()) ? id : (partialPath + "/" + id), props.get("title"), path, menu_order);
                }
            }
        } catch (IOException iOException) {
            //
        }
        return null;
    }

    private DocusaurusFile(Type type, String shortId, String longId, String title, Path path, String content, MdElement tree, int order) {
        this.type = type;
        this.shortId = shortId;
        this.longId = longId;
        this.title = title;
        this.path = path;
        this.content = content;
        this.tree = tree;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public Type getType() {
        return type;
    }

    public String getLongId() {
        return longId;
    }

    public String getShortId() {
        return shortId;
    }

    public String getTitle() {
        return title;
    }

    public Path getPath() {
        return path;
    }

    public String getContent() {
        return content;
    }

    public MdElement getTree() {
        return tree;
    }

    @Override
    public String toString() {
        String s=longId;
        if(!s.startsWith("/")){
            s="/"+s;
        }
        return s;
    }
    public enum Type {
        PATH,
        CONTENT,
        TREE,
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    public String toJSON(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(DocusaurusUtils.indentChars(indent));
        sb.append("'").append(DocusaurusUtils.escapeString(getLongId() + "'"));
        return sb.toString();
    }
}
