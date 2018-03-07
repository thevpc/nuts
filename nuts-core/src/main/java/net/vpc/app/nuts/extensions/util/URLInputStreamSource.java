/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import net.vpc.app.nuts.NutsIOException;

/**
 *
 * @author vpc
 */
class URLInputStreamSource implements InputStreamSource {

    private final URL url;

    public URLInputStreamSource(URL url) {
        this.url = url;
    }

    @Override
    public InputStream openStream() {
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    @Override
    public String getName() {
        return CoreIOUtils.getURLName(url);
    }

    @Override
    public Object getSource() {
        return url;
    }

    @Override
    public String toString() {
        return "URLInputStreamSource{" + "url=" + url + '}';
    }

}
