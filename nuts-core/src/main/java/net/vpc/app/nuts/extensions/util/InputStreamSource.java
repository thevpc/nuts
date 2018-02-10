package net.vpc.app.nuts.extensions.util;

import java.io.InputStream;

public interface InputStreamSource {

    InputStream openStream();

    String getName();

    Object getSource();
}
