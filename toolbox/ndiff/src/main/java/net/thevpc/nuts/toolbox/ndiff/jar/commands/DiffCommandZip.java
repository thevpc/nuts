/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndiff.jar.commands;

import net.thevpc.nuts.toolbox.ndiff.jar.*;
import net.thevpc.nuts.toolbox.ndiff.jar.util.DiffUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author thevpc
 */
public class DiffCommandZip extends AbstractDiffCommand {
    public static final DiffCommandZip INSTANCE = new DiffCommandZip();
//    private File source;
//    private File target;
//    private Predicate<String> pathFilter;
//    private transient ZipFile zfile1;
//    private transient ZipFile zfile2;


    protected DiffCommandZip() {
        super("zip");
    }

    protected DiffCommandZip(String id) {
        super(id);
    }


    @Override
    public int acceptInput(Object input) {
//        if (input instanceof InputStream) {
//            return 1;
//        }
//        if (input instanceof byte[]) {
//            return 1;
//        }
        if (input instanceof File) {
            File f = ((File) input);
            String n = f.getName().toLowerCase();
            if (n.endsWith(".zip")) {
                return 100;
            }
            if (n.endsWith(".jar") || n.endsWith(".war") || n.endsWith(".ear")) {
                return 50;
            }
        }
        return -1;
    }

    @Override
    public Object prepareSourceOrTarget(Object source) {
        if (source instanceof File) {
            return new ZipSourceFile((File) source);
        }
        if (source instanceof ZipSource) {
            return source;
        }
        throw new IllegalArgumentException("Unsupported zip from " + source);
    }

    protected boolean acceptEntry(String entryName) {
        return true;
    }

    @Override
    public boolean acceptDiffKey(DiffKey key) {
        switch (key.getKind()) {
            case DiffKey.KIND_FILE: {
                return key.getName().endsWith(".zip");
            }
        }
        return false;
    }

    @Override
    public Map<DiffKey, String> map(Object item, DiffEvalContext diffEvalContext) {
        HashMap<DiffKey, String> t = new HashMap<>();
        if (item instanceof ZipSourceFile) {
            try {
                ZipFile zipFile = ((ZipSourceFile) item).getZfile();
                Enumeration e = zipFile.entries();
                Predicate<String> pathFilter = diffEvalContext.getPathFilter();
                while (e.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    String entryName = entry.getName();
                    if (!entryName.endsWith("/")) {
                        if (pathFilter == null || pathFilter.test(entryName)) {
                            if(!diffEvalContext.isDefaultPathFilterEnabled() || acceptEntry(entryName)) {
                                try (InputStream is = zipFile.getInputStream(entry)) {
                                    DiffCommand factory = resolveDiffItemFactory(new DiffKey(entryName, DiffKey.KIND_FILE, 0), diffEvalContext);
                                    String ss = factory.hash(is);
                                    if (ss != null) {
                                        t.put(new DiffKey(entryName, DiffKey.KIND_FILE, 0), ss);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }else{
            throw new IllegalArgumentException("Unsupported source "+item);
        }
        return t;
    }

    @Override
    public String hash(InputStream source) {
        return DiffUtils.hashStream(source);
    }

    @Override
    public DiffItem createContentDiffItem(DiffItemCreateContext context) {
        switch (context.getStatus()) {
            case ADDED: {
                return new DiffItemJavaClass(context.getKey().getName(), DiffStatus.ADDED, null, null);
            }
            case REMOVED: {
                return new DiffItemJavaClass(context.getKey().getName(), DiffStatus.REMOVED, null, null);
            }
            case CHANGED: {
                return new DiffItemJavaClass(context.getKey().getName(), DiffStatus.CHANGED, null/*sourceValue+" -> "+targetValue*/, null);
            }
        }
        throw new UnsupportedOperationException();
    }

    public interface ZipSource extends AutoCloseable {
        InputStream getInputStream(String name);
    }

    public class ZipSourceFile implements ZipSource {
        private File file;
        private ZipFile zfile;

        public ZipSourceFile(File file) {
            this.file = file;
        }

        public ZipFile getZfile() {
            if (zfile == null) {
                try {
                    this.zfile = new ZipFile(file);
                } catch (IOException e) {
                    //
                }
            }
            return zfile;
        }

        @Override
        public InputStream getInputStream(String name) {
            try {

                return getZfile().getInputStream(getZfile().getEntry(name));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void close() throws Exception {
            if (zfile != null) {
                zfile.close();
            }
        }
    }
}
