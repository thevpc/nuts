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

    public static final String EDITOR_NNOTE_DOCUMENT = "nnote-document";
    public static final String EDITOR_NOTE_LIST = "note-list";
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
    public static final String NOTE_LIST = "application/nnote-list";
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
                    NOTE_LIST,
                    OBJECT_LIST
            )
    );

    public static Set<String> ALL_USER_ICONS = new TreeSet<String>(
            Arrays.asList(
                    "bell",
                    "book",
                    "circle",
                    "clock",
                    "coffee",
                    "database",
                    "disc",
                    "file",
                    "file",
                    "file-c",
                    "file-cpp",
                    "file-html",
                    "file-java",
                    "file-javascript",
                    "file-markdown",
                    "file-nnote",
                    "file-nuts-text-format",
                    "file-text",
                    "gift",
                    "heart",
                    "moon",
                    "network",
                    "nnote-list",
                    "nnote-object-list",
                    "password",
                    "phone",
                    "smile",
                    "star",
                    "string",
                    "sun",
                    "url",
                    "wifi",
                    "datatype-audio",
                    "datatype-calendar",
                    "datatype-chart",
                    "datatype-checkbox",
                    "datatype-combobox",
                    "datatype-email",
                    "datatype-image",
                    "datatype-link",
                    "datatype-list",
                    "datatype-map",
                    "datatype-money",
                    "datatype-number",
                    "datatype-numbered-list",
                    "datatype-password",
                    "datatype-pen",
                    "datatype-phone",
                    "datatype-tags",
                    "datatype-text",
                    "datatype-textarea",
                    "datatype-url",
                    "datatype-video"
            )
    );

}
