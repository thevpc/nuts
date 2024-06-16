package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.NCodeHighlighter;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.*;

import java.util.*;

public class HalfaCodeHighlighter extends TsonCodeHighlighter {

    public HalfaCodeHighlighter(NWorkspace ws) {
        super(ws);
    }

    @Override
    public String getId() {
        return "halfa";
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        String s = context.getConstraints();
        if (s == null) {
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        switch (s) {
            case "halfa":
            case "hd":
            case "application/halfa":
            case "application/hd":
            case "text/halfa":
            case "text/hd": {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

    public static final Set<String> p1 = new HashSet<>(Arrays.asList(
            "import;page;page-group;styles"
                    .split(";")
    ));
    public static final Set<String> p2 = new HashSet<>(Arrays.asList(
            "arc;circle;ellipse;eq;equation;flow;grid;image;img;line;octagon;ol;pentagon;plain;points;polygon;polyline;rectangle;sphere;square;stack;text;triangle;txt;ul;void"
                    .split(";")
    ));
    public static final Set<String> p3 = new HashSet<>(Arrays.asList(
            "anchor;at;background;bg;bottom;class;color;columns;debug;disabled;draw-contour;fg;fill;font-bold;font-crossthrough;font-family;font-italic;font-size;font-underline;grid-color;hide;left;line-color;name;origin;position;preserve-aspect-ratio;right;rotate;rows;show;size;sphere;stroke;template;top;weights"
                    .split(";")
    ));

    @Override
    protected NTextStyle resolveTokenStyle(String token) {
        if(p1.contains(token)){
            return NTextStyle.primary1();
        }
        if(p2.contains(token)){
            return NTextStyle.primary2();
        }
        if(p3.contains(token)){
            return NTextStyle.primary3();
        }
        return super.resolveTokenStyle(token);
    }
}
