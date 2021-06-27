package net.thevpc.nuts.toolbox.ncode.filters;

import net.thevpc.nuts.toolbox.ncode.bundles.strings.StringComparator;
import net.thevpc.nuts.toolbox.ncode.sources.JavaTypeSource;
import net.thevpc.nuts.toolbox.ncode.Source;
import net.thevpc.nuts.toolbox.ncode.SourceFilter;
import net.thevpc.nuts.toolbox.ncode.sources.ZipEntrySource;
import net.thevpc.nuts.toolbox.ncode.sources.ZipSource;

import java.util.zip.ZipEntry;

public class JavaSourceFilter implements SourceFilter {

    private final StringComparator type;
    private final StringComparator file;

    public JavaSourceFilter(StringComparator type, StringComparator file) {
        this.type = type;
        this.file = file;
    }

    @Override
    public boolean accept(Source source) {
        if (source instanceof JavaTypeSource) {
            JavaTypeSource s = (JavaTypeSource) source;
            if (file == null || file.matches(source.getExternalPath())) {
                String n = s.getClassName();
                if (type == null || type.matches(n)) {
                    return true;
                }
            }
        }else if (source instanceof ZipSource) {
            ZipSource s = (ZipSource) source;
            if (file == null || file.matches(source.getExternalPath())) {
                String n = s.getName();
                if(n.endsWith(".class")) {
                    n=n.substring(0,n.length()-".class".length());
                    String[] q = n.split("[$]");
                    if (type == null || type.matches(q[0])) {
                        return true;
                    }
                }
            }
        }else if (source instanceof ZipEntrySource) {
            ZipEntrySource s = (ZipEntrySource) source;
            if (file == null || file.matches(source.getExternalPath())) {
                String n = s.getName();
                if(n.endsWith(".class")) {
                    n=n.substring(0,n.length()-".class".length());
                    String[] q = n.split("[$]");
                    if (type == null || type.matches(q[0])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean lookInto(Source source) {
        return true;
    }
}
