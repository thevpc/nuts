/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import net.thevpc.nuts.toolbox.nnote.model.NNote;

/**
 *
 * @author vpc
 */
public class NNoteTypes {

    public static final String EDITOR_N_NOTE_DOCUMENT = "nnote-document";
    public static final String EDITOR_NODE_LIST = "node-list";
    public static final String EDITOR_OBJECT_LIST = "object-list";
    public static final String EDITOR_FILE = "file";
    public static final String EDITOR_URL = "url";
    public static final String EDITOR_STRING = "string";
    public static final String EDITOR_PASSWORD = "password";
    public static final String EDITOR_WYSIWYG = "wysiwyg";
    public static final String EDITOR_SOURCE = "source";
    public static final String EDITOR_UNSUPPORTED = "unsupported";

    public static final String UNSUPPORTED = "application/unsupported";
    public static final String PLAIN = "text/plain";
    public static final String HTML = "text/html";
    public static final String MARKDOWN = "text/markdown";
    public static final String NUTS_TEXT_FORMAT = "text/nuts-text-format";
    public static final String JAVA = "text/java";
    public static final String JAVASCRIPT = "text/javascript";
    public static final String C = "text/c";
    public static final String CPP = "text/cpp";
    public static final String SH = "text/sh";
    public static final String NNOTE_DOCUMENT = "application/nnote-document";
    public static final String FILE = "application/nnote-file";
    public static final String URL = "application/nnote-url";
    public static final String PASSWORD = "application/nnote-password";
    public static final String STRING = "application/nnote-string";
    public static final String NODE_LIST = "application/nnote-node-list";
    public static final String OBJECT_LIST = "application/nnote-object-list";

    public static final String RICH_HTML = "text/html:" + EDITOR_WYSIWYG;
    public static final String RICH_MARKDOWN = "text/markdown:" + EDITOR_WYSIWYG;
    public static final String RICH_NUTS_TEXT_FORMAT = "text/nuts-text-format:" + EDITOR_WYSIWYG;
    public static final String SOURCE_HTML = "text/html:" + EDITOR_SOURCE;
    public static final String SOURCE_MARKDOWN = "text/markdown:" + EDITOR_SOURCE;
    public static final String SOURCE_NUTS_TEXT_FORMAT = "text/nuts-text-format:" + EDITOR_SOURCE;

    public static Set<String> ALL_CONTENT_TYPES = new LinkedHashSet<String>(
            Arrays.asList(PLAIN,
                    HTML,
                    MARKDOWN,
                    NUTS_TEXT_FORMAT,
                    JAVA,
                    JAVASCRIPT,
                    C,
                    CPP,
                    NNOTE_DOCUMENT,
                    FILE,
                    URL,
                    PASSWORD,
                    STRING,
                    NODE_LIST,
                    OBJECT_LIST
            )
    );

    public static Set<String> ALL_USER_ICONS = new TreeSet<String>(
            Arrays.asList(
                    "file-html",
                    "file-markdown",
                    "file-java",
                    "file-javascript",
                    "file-c",
                    "file-cpp",
                    "file-nnote",
                    "file",
                    "star",
                    "circle",
                    "gift",
                    "heart",
                    "sun",
                    "moon",
                    "smile",
                    "coffee",
                    "clock",
                    "bell",
                    "book",
                    "disc",
                    "database"
            )
    );

    public static boolean isValidIcon(String icon) {
        if (icon == null) {
            return false;
        }
        return ALL_USER_ICONS.contains(icon);
    }

    public static String normalizeIcon(NNote node, boolean folder, boolean expanded) {
        String icon;
        if (folder) {
            icon = node.getFolderIcon();
            icon = icon == null ? "" : icon.toLowerCase().trim();
            if (isValidIcon(icon)) {
                return icon;
            }
        }
        icon = node.getIcon();
        icon = icon == null ? "" : icon.toLowerCase().trim();
        if (isValidIcon(icon)) {
            return icon;
        }
        if (folder) {
            if (expanded) {
                return "folder-open";
            } else {
                return "folder-closed";
            }
        }
        String contentType = normalizeContentType(node.getContentType());
        switch (contentType) {
            case PLAIN:
                return "file-text";
            case HTML:
                return "file-html";
            case MARKDOWN:
                return "file-markdown";
            case NUTS_TEXT_FORMAT:
                return "file-nuts-text-format";
            case JAVA:
                return "file-java";
            case JAVASCRIPT:
                return "file-javascript";
            case C:
                return "file-c";
            case CPP:
                return "file-cpp";
            case NNOTE_DOCUMENT:
                return "file-nnote";
            case FILE:
                return "file";
            case URL:
                return "url";
            case PASSWORD:
                return "password";
            case STRING:
                return "string";
            case NODE_LIST:
                return "nnote-node-list";
            case OBJECT_LIST:
                return "nnote-object-list";
        }
        return "unknown";
    }

    public static String normalizeContentType(String ct) {
        if (ct == null) {
            ct = "";
        }
        ct = ct.trim().toLowerCase();
        if (ct.isEmpty()) {
            ct = PLAIN;
        }
        if (ALL_CONTENT_TYPES.contains(ct)) {
            return ct;
        }
        if (ct.contains(":")) {
            ct = ct.substring(0, ct.indexOf(':'));
        }
        if (!ct.contains("/")) {
            for (String t : ALL_CONTENT_TYPES) {
                if (t.endsWith("/" + ct)) {
                    return t;
                }
            }
        }
        return UNSUPPORTED;
    }

    public static String[] getEditorTypes(String contentType) {
        return normalizeEditorTypes(contentType, null);
    }

    public static String normalizeEditorType(String contentType, String editorType) {
        return normalizeEditorTypes(contentType, editorType)[0];
    }

    public static String[] normalizeEditorTypes(String contentType, String editorType) {
        if (editorType == null) {
            editorType = "";
        }
        editorType = editorType.trim().toLowerCase();
        switch (normalizeContentType(contentType)) {
            case HTML:
            case NUTS_TEXT_FORMAT:
            case MARKDOWN: {
                if (editorType.isEmpty()) {
                    return new String[]{EDITOR_WYSIWYG, EDITOR_SOURCE};
                }
                switch (editorType) {
                    case EDITOR_WYSIWYG: {
                        return new String[]{EDITOR_WYSIWYG};
                    }
                    case EDITOR_SOURCE: {
                        return new String[]{EDITOR_SOURCE};
                    }
                    default: {
                        return new String[]{EDITOR_WYSIWYG};
                    }
                }
            }
            case PLAIN:
            case JAVA:
            case C:
            case CPP:
            case JAVASCRIPT: {
                return new String[]{EDITOR_SOURCE};
            }
            case NODE_LIST: {
                return new String[]{EDITOR_NODE_LIST};
            }
            case OBJECT_LIST: {
                return new String[]{EDITOR_OBJECT_LIST};
            }
            case STRING: {
                return new String[]{EDITOR_STRING};
            }
            case PASSWORD: {
                return new String[]{EDITOR_PASSWORD};
            }
            case FILE: {
                return new String[]{EDITOR_FILE};
            }
            case URL: {
                return new String[]{EDITOR_URL};
            }
            case NNOTE_DOCUMENT: {
                return new String[]{EDITOR_N_NOTE_DOCUMENT};
            }
            default: {
                return new String[]{EDITOR_UNSUPPORTED};
            }
        }
    }
}
