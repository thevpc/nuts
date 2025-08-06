package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NColors;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NNamedColor;

import java.util.*;

public class NTexupCodeHighlighter extends TsonCodeHighlighter {
    public static final Set<String> ctrl = new HashSet<>(Arrays.asList(
            "import;page;page-group;styles;if;for"
                    .split(";")
    ));
    //    public static final Set<String> p2 = new HashSet<>(Arrays.asList(
//            "arc;circle;ellipse;eq;equation;flow;grid;image;img;line;octagon;ol;pentagon;plain;points;polygon;polyline;rectangle;sphere;square;stack;text;triangle;txt;ul;void"
//                    .split(";")
//    ));
//    public static final Set<String> p3 = new HashSet<>(Arrays.asList(
//            "anchor;at;background;bg;bottom;class;color;columns;debug;disabled;draw-contour;fg;fill;font-bold;font-crossthrough;font-family;font-italic;font-size;font-underline;grid-color;hide;left;line-color;name;origin;position;preserve-aspect-ratio;right;rotate;rows;show;size;sphere;stroke;template;top;weights"
//                    .split(";")
//    ));
    public static final Set<String> attrs = new HashSet<>(Arrays.asList(
            "anchor;at;background;bg;bottom;class;color;columns;debug;disabled;draw-contour;fg;fill;font-bold;font-crossthrough;margin;columns-weight;from;to;ctrl;ctrl1;start-arrow;end-arrow;rows-weight;font-family;font-italic;font-size;font-underline;grid-color;hide;left;line-color;name;origin;position;preserve-aspect-ratio;right;rotate;rows;show;size;sphere;stroke;top;weights"
                    .split(";")
    ));
    public static final Set<String> components = new HashSet<>(Arrays.asList(
            "arc;circle;ellipse;eq;equation;flow;grid;image;img;line;octagon;ol;pentagon;plain;points;polygon;polyline;rectangle;sphere;square;group;text;triangle;txt;ul;void;ntf;source;col;column;row;rhombus;diamond;parallelogram;trapezoid;quad-curve;cubic-curve;plot2d;plot3d;hexagon;helptagon;nonagon;decagon;arrow;donut;cylinder;pie;image;gantt;nwdiag;uml;wireframe;"
                    .split(";")
    ));
    public static final Set<String> values = new HashSet<>(Arrays.asList(
            "center;left;right;true;false;red"
                    .split(";")
    ));

    static {
        for (NNamedColor c : NColors.ALL) {
            values.add(c.getName());
            values.add(NNameFormat.VAR_NAME.format(c.getName()));
            values.add(NNameFormat.SNAKE_CASE.format(c.getName()));
        }
    }

    public NTexupCodeHighlighter() {
        super();

    }

    @Override
    public String getId() {
        return "ntexup";
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        String s = context.getConstraints();
        if (s == null) {
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        switch (s) {
            case "ntexup":
            case "application/ntexup":
            case "text/ntexup": {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }


    @Override
    protected NTextStyles resolveTokenStyle(String token, String next, NText last) {
        if ("(".equals(next) || "{".equals(next)) {
            if (ctrl.contains(token)) {
                return NTextStyles.of(NTextStyle.primary2());
            }
            if (components.contains(token)) {
                return NTextStyles.of(NTextStyle.primary1());
            }
            return NTextStyles.of(NTextStyle.primary1());
        }
        if (":".equals(next)) {
            if (attrs.contains(token)) {
                return NTextStyles.of(NTextStyle.primary3());
            }
            return NTextStyles.of(NTextStyle.primary3());
        }
        if (last != null) {
            String s = last.filteredText();
            if (s.equals(":")) {
                if (values.contains(token)) {
                    return NTextStyles.of(NTextStyle.primary4());
                }
                return NTextStyles.of(NTextStyle.primary4());
            }
            if (attrs.contains(token)) {
                return NTextStyles.of(NTextStyle.primary3());
            }
        }
        return super.resolveTokenStyle(token, next, last);
    }
}
