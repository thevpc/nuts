package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.text.*;

import java.util.*;

/**
 * Syntax highlighter for ntexup documents ({@code .ntx}, historically also
 * called {@code ntexup}). ntexup documents are TSON documents whose node names
 * are slide/page elements and whose named arguments are element properties.
 */
public class NTexupCodeHighlighter extends TsonCodeHighlighter {

    /**
     * Control words that introduce a statement or a block.
     */
    public static final Set<String> ctrl = new LinkedHashSet<>(Arrays.asList(
            "import", "include", "page", "page-group", "styles", "define", "if", "else", "for", "while",
            "return", "let", "var", "fn", "function", "component"
    ));

    /**
     * Built-in ntexup node types (core extensions). These are stable names
     * understood by a raw ntexup document without any theme.
     */
    public static final Set<String> defaultNodes = new LinkedHashSet<>(Arrays.asList(
            // structure
            "page-group", "page", "fragment", "group", "block", "flow", "grid", "row", "column",
            "table", "table-row", "table-cell", "filler", "void", "stack",
            // text
            "plain", "text", "txt", "ntf", "source", "eq", "equation",
            // shapes 2d
            "rectangle", "square", "circle", "ellipse", "triangle", "pentagon", "hexagon", "heptagon",
            "octagon", "nonagon", "decagon", "rhombus", "trapezoid", "parallelogram", "polygon", "polyline",
            "line", "arrow", "connector", "arc", "quad-curve", "cubic-curve", "donut", "pie", "cylinder",
            "sphere", "ellipsoid", "scene3d", "box", "box3d", "pyramid", "pyramid3d", "uvsphere", "sphere3d",
            "helix", "helix3d", "spiral3d", "polyline3d", "polygon3d",
            // layout shortcuts and media
            "ordered-list", "unordered-list", "ol", "ul", "col", "diamond", "rect", "bar", "img", "picture",
            "image", "plot2d", "plot3d", "table",
            // other built-in extensions
            "scoreboard", "score-board", "leaderboard", "scores",
            "progress-monitor", "progress-view",
            // diagrams
            "plantuml", "uml", "gantt", "nwdiag", "wireframe", "mindmap", "salt", "ditaa", "chen",
            "ebnf", "chronology", "wbs", "regex", "plantuml-json", "json-diagram",
            "plantuml-yaml", "yaml-diagram",
            // control nodes
            "assign", "defaultassign", "expr", "call", "for", "define", "include", "uncompiled", "import",
            "ctrlname", "ctrlcomplexresult", "ctrlsimpleresult", "ctrlerror", "if", "unnamed"
    ));

    /**
     * Backward compatible alias for {@link #defaultNodes}.
     */
    public static final Set<String> components = defaultNodes;

    /**
     * Components defined by the bundled themes (classic, eniso, ibtihel,
     * meridian). They are highlighted with a color of their own so a reader can
     * tell theme-provided slide components from core elements.
     */
    public static final Set<String> themeComponents = new LinkedHashSet<>(Arrays.asList(
            "agenda-slide", "cover-slide", "content-slide", "conclusion-slide", "thankyou-slide",
            "body", "body2a", "body2b", "body3a", "body3b", "body3c",
            "header", "footer", "card", "kicker", "divider", "stat", "quote", "panel", "callout",
            "hint-info", "hint-warning", "hint-error", "hint-success", "image-fig",
            "definition", "example", "remark", "theorem", "palette", "Palette"
    ));

    /**
     * Common ntexup property names.
     */
    public static final Set<String> attrs = new LinkedHashSet<>(Arrays.asList(
            "anchor", "at", "background", "bg", "bottom", "class", "color", "columns", "columns-weight",
            "ctrl", "ctrl1", "debug", "disabled", "draw-contour", "end-arrow", "fg", "fill", "font-bold",
            "font-crossthrough", "font-family", "font-italic", "font-size", "font-underline", "from",
            "grid-color", "hide", "left", "line-color", "margin", "name", "origin", "position",
            "preserve-aspect-ratio", "right", "rotate", "rows", "rows-weight", "show", "size", "sphere",
            "start-arrow", "stroke", "template", "to", "top", "weights"
    ));

    /**
     * Common bare scalar values (flags, alignments, sizes...).
     */
    public static final Set<String> values = new LinkedHashSet<>(Arrays.asList(
            "all", "auto", "baseline", "bold", "bottom", "both", "center", "contain", "cover",
            "clockwise", "counter-clockwise", "dashed", "dotted", "end", "false", "fill", "hidden",
            "horizontal", "italic", "justify", "large", "left", "medium", "middle", "none", "no-repeat",
            "nowrap", "null", "repeat", "right", "row", "column", "small", "solid", "start", "stretch",
            "top", "true", "underline", "vertical", "visible", "wrap", "xlarge", "xsmall"
    ));

    static {
        for (NColor c : NColor.ALL) {
            values.add(c.name());
            values.add(NNameFormat.VAR_NAME.format(c.name()));
            values.add(NNameFormat.SNAKE_CASE.format(c.name()));
        }
    }

    public NTexupCodeHighlighter() {
        super();
    }

    @Override
    public String id() {
        return "ntexup";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if (s == null) {
            return NScorable.DEFAULT_SCORE;
        }
        switch (s) {
            case "ntexup":
            case "application/ntexup":
            case "text/ntexup": {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    protected NTextStyles resolveTokenStyle(String token, String next, NText last) {
        if ("true".equals(token) || "false".equals(token)) {
            return NTextStyles.of(NTextStyle.bool());
        }
        if ("null".equals(token)) {
            return NTextStyles.of(NTextStyle.keyword());
        }
        if ("(".equals(next) || "{".equals(next)) {
            if (ctrl.contains(token)) {
                return NTextStyles.of(NTextStyle.primary2());
            }
            if (themeComponents.contains(token)) {
                return NTextStyles.of(NTextStyle.primary5());
            }
            if (defaultNodes.contains(token)) {
                return NTextStyles.of(NTextStyle.primary1());
            }
            if (attrs.contains(token)) {
                return NTextStyles.of(NTextStyle.primary3());
            }
            // user defined components (themes, @define) are call-like too
            return NTextStyles.of(NTextStyle.primary6());
        }
        if (":".equals(next)) {
            // object/node property
            return NTextStyles.of(NTextStyle.primary3());
        }
        if (last != null) {
            String s = last.filteredText();
            if (":".equals(s)) {
                // value position
                return NTextStyles.of(NTextStyle.primary4());
            }
        }
        if (attrs.contains(token)) {
            return NTextStyles.of(NTextStyle.primary3());
        }
        if (values.contains(token)) {
            return NTextStyles.of(NTextStyle.primary4());
        }
        return super.resolveTokenStyle(token, next, last);
    }
}
