package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.util.regex.Pattern;

public interface TsonRegex extends TsonElement {
    Pattern value();

    TsonPrimitiveBuilder builder();
}
