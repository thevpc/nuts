/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.nuts.toolbox.feenoo;

import net.vpc.common.strings.StringComparator;
import net.vpc.nuts.toolbox.feenoo.sources.JavaTypeSource;

/**
 *
 * @author vpc
 */
class JavaTypeLookup implements SourceProcessor {

    private final StringComparator type;
    private final StringComparator file;

    public JavaTypeLookup(StringComparator comparator, StringComparator file) {
        this.type = comparator;
        this.file = file;
    }

    @Override
    public boolean process(Source source) {
        if (source instanceof JavaTypeSource) {
            JavaTypeSource s = (JavaTypeSource) source;
            if (file == null || file.matches(source.getExternalPath())) {
                String n = s.getClassName();
                if (type.matches(n)) {
                    System.out.println(n + "   " + source.getExternalPath());
                }
            }
        }
        return true;
    }

}
