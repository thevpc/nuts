package net.thevpc.nuts.runtime.standalone.tson;

import java.util.regex.Pattern;

public interface TsonRegex extends TsonElement {
    Pattern value();

    TsonPrimitiveBuilder builder();
}
