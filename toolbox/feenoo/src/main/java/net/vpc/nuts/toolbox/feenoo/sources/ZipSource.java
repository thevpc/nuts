/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.nuts.toolbox.feenoo.sources;

import net.vpc.nuts.toolbox.feenoo.Source;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author vpc
 */
public class ZipSource extends SourceAdapter {

    public ZipSource(Source source) {
        super(source);
    }

    @Override
    public Iterable<Source> getChildren() {
        return new ZipIterable();
    }

    protected ZipEntrySource createZipSource(ZipEntry nextEntry, ZipInputStream zip) {
        return new ZipEntrySource(nextEntry, zip, getSource().getExternalPath());
    }

    @Override
    public boolean isStream() {
        return true;
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new ZipInputStream(getSource().openStream());
    }


    private class ZipIterable implements Iterable<Source> {

        @Override
        public Iterator<Source> iterator() {
            try {
                return new ZipIterator(getSource().openStream());
            } catch (IOException ex) {
                return new ZipIterator(null);
            }
        }

    }

    private class ZipIterator implements Iterator<Source>, Closeable {

        InputStream in;
        JarInputStream zip = null;
        ZipEntry nextEntry = null;

        public ZipIterator(InputStream in) {
            this.in = in;
            try {
                zip = in == null ? null : new JarInputStream(in);
            } catch (Exception ex) {
                Logger.getLogger(ZipSource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public boolean hasNext() {
            if (zip != null) {
                try {
                    nextEntry = zip.getNextEntry();
                    if (nextEntry == null) {
                        close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ZipSource.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                nextEntry = null;
            }
            return nextEntry != null;
        }

        public void close() {
            try {
                zip.close();
            } catch (IOException ex) {
                //    
            }
        }

        @Override
        public Source next() {
            return SourceFactory.wrap(createZipSource(nextEntry, zip));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

}
