package net.thevpc.nuts.runtime.standalone.tson;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

public interface TsonCustomBuilder extends TsonElementBuilder {
    TsonCustomBuilder setCustom(Object element);

    Object getCustom();

    TsonElement build();

    ////////////////////////////////////////////////

    TsonCustomBuilder comments(TsonComments comments);

    TsonCustomBuilder setComments(TsonComments comments);

    TsonCustomBuilder setAnnotations(TsonAnnotation... annotations);

    TsonCustomBuilder addAnnotations(TsonAnnotation... annotations);

    TsonCustomBuilder addAnnotations(Collection<TsonAnnotation> annotations);

    TsonCustomBuilder annotation(String name, TsonElementBase... elements);

    TsonCustomBuilder addAnnotation(String name, TsonElementBase... elements);

    TsonCustomBuilder addAnnotation(TsonAnnotation annotation);

    TsonCustomBuilder removeAnnotationAt(int index);

    TsonCustomBuilder clearAnnotations();
}
