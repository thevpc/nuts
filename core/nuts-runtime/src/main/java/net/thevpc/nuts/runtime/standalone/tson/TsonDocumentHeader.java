package net.thevpc.nuts.runtime.standalone.tson;

import java.util.List;

public interface TsonDocumentHeader extends Comparable<TsonDocumentHeader>{
    String getVersion();

    String getEncoding();

    int size();

    List<TsonElement> all();

    List<TsonElement> getAll();

    TsonDocumentHeaderBuilder builder();

}
