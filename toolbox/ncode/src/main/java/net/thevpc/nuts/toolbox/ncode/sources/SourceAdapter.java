/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode.sources;

import net.thevpc.nuts.toolbox.ncode.Source;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author thevpc
 */
public class SourceAdapter implements Source {

    private Source source;

    public SourceAdapter(Source source) {
        this.source = source;
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @Override
    public String getExternalPath() {
        return source.getExternalPath();
    }

    @Override
    public Iterable<Source> getChildren() {
        return source.getChildren();
    }

    @Override
    public String getInternalPath() {
        return source.getInternalPath();
    }

    @Override
    public boolean isStream() {
        return source.isStream();
    }

    @Override
    public boolean isFolder() {
        return source.isFolder();
    }

    @Override
    public InputStream openStream() throws IOException {
        return source.openStream();
    }

    public Source getSource() {
        return source;
    }

}
