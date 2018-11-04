/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.nuts.toolbox.feenoo.sources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.vpc.nuts.toolbox.feenoo.Source;

/**
 *
 * @author vpc
 */
public class RegularFileSource extends FileSource {

    public RegularFileSource(File file) {
        super(file);
    }

    @Override
    public List<Source> getChildren() {
        List<Source> found = new ArrayList<>();
        File[] files = getFile().listFiles();
        if (files != null) {
            for (File f : files) {
                found.add(SourceFactory.create(f));
            }
        }
        return found;
    }

    @Override
    public boolean isStream() {
        return true;
    }

}
