package net.vpc.app.nuts.extensions.util;

import java.io.IOException;
import java.io.InputStream;

public interface StreamVisitor {
    boolean visit(String path,InputStream inputStream) throws IOException;
}
