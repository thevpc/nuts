package net.thevpc.nuts.lib.doc.executor;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.NDocExprEvaluator;
import net.thevpc.nuts.lib.doc.mimetype.MimeTypeConstants;
import net.thevpc.nuts.lib.doc.processor.impl.DefaultNDocStreamExecutor;
import net.thevpc.nuts.lib.doc.util.FileProcessorUtils;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class NDocExecutorManager {
    private static final NDocExecutor NEXPR_EXECUTOR = new StreamToExecutor(new DefaultNDocStreamExecutor(new NDocExprEvaluator()));
    public static final NDocExecutor DEFAULT_EXECUTOR = NEXPR_EXECUTOR;
    private static final NDocExecutor IGNORE_EXECUTOR = new IgnoreNDocExecutor();

    private static final Map<String, NDocExecutor> globalExecProcessorsByMimeType = new HashMap<>();
    static {
        globalExecProcessorsByMimeType.put(MimeTypeConstants.NEXPR, NEXPR_EXECUTOR);
        globalExecProcessorsByMimeType.put(MimeTypeConstants.IGNORE, IGNORE_EXECUTOR);
    }

    private NDocExecutorFactory executorFactory;
    private final Map<String, NDocExecutor> execProcessorsByMimeType = new HashMap<>();
    private NDocContext context;

    public NDocExecutorManager(NDocContext context) {
        this.context = context;
    }

    public NOptional<NDocExecutor> getExecutor(Path path, String mimeTypesString) {
        String[] mimeTypesArray = mimeTypesString == null ? FileProcessorUtils.splitMimeTypes(context.getMimeTypeResolver().resolveMimetype(path.toString()))
                : FileProcessorUtils.splitMimeTypes(mimeTypesString);
        for (String mimeType : mimeTypesArray) {
            NDocExecutor proc = null;
            try {
                proc = getExecutor(mimeType);
                if(proc!=null){
                    return NOptional.of(proc);
                }
            } catch (Exception ex) {
                context.getLog().error("file", "error resolving executor for mimetype " + mimeType + " and file : " + path.toString() + ". " + ex.toString());
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("executor for %s %s",mimeTypesString,path.toString()));
    }

    public NDocExecutor getExecutor(String mimetypes) {
        String[] mimeTypes = FileProcessorUtils.splitMimeTypes(mimetypes);
        for (String mimeType : mimeTypes) {
            if (mimeType == null || mimeType.isEmpty() || mimeType.equals("*")) {
                mimeType = MimeTypeConstants.ANY_TYPE;
            }
            String[] mts = Stream.of(mimeType.split(";")).map(String::trim).filter(x -> x.length() > 0).toArray(String[]::new);
            for (String mt : mts) {
                NDocExecutor m = getExecutorExact(mt);
                if (m != null) {
                    return m;
                }
            }
            for (String mt : mts) {
                int slash = mt.indexOf('/');
                if (slash > 0) {
                    String a = mt.substring(0, slash);
                    String b = mt.substring(slash + 1);
                    if (!b.equals("*")) {
                        NDocExecutor m = getExecutorExact(a + "/*");
                        if (m != null) {
                            return m;
                        }
                    }
                }
            }
            NDocExecutor z = getExecutorExact(MimeTypeConstants.ANY_TYPE);
            if (z != null) {
                return z;
            }
        }
        return DEFAULT_EXECUTOR;
    }

    public NDocExecutor getExecutorExact(String mimetype) {
        NDocExecutorFactory executorFactory = getExecutorFactory();
        if (executorFactory != null) {
            NDocExecutor p = executorFactory.getExecutor(mimetype);
            if (p != null) {
                return p;
            }
        }
        NDocExecutor p = execProcessorsByMimeType.get(mimetype);
        if (p != null) {
            return p;
        }
        if (context.getParent() != null) {
            return context.getParent().getExecutorManager().getExecutorExact(mimetype);
        }
        p = globalExecProcessorsByMimeType.get(mimetype);
        if (p != null) {
            return p;
        }
        return null;
    }

    public NDocExecutor getDefaultExecutor(String mimetype) {
        NDocExecutor processor = execProcessorsByMimeType.get(mimetype);
        if (processor != null) {
            return processor;
        }
        if (context.getParent() != null) {
            return context.getParent().getExecutorManager().getDefaultExecutor(mimetype);
        }
        return null;
    }

    public NDocExecutorManager setDefaultExecutor(String mimetype, net.thevpc.nuts.lib.doc.executor.NDocExprEvaluator executor) {
        return setDefaultExecutor(mimetype, executor == null ? null : new StreamToExecutor(new DefaultNDocStreamExecutor(executor)));
    }

    public NDocExecutorManager setDefaultExecutor(String mimetype, NDocExecutor executor) {
        if (executor == null) {
            execProcessorsByMimeType.remove(mimetype);
        } else {
            execProcessorsByMimeType.put(mimetype, executor);
        }
        return this;
    }

    public NDocExecutorFactory getExecutorFactory() {
        if (executorFactory != null) {
            return executorFactory;
        }
        if (context.getParent() != null) {
            return context.getParent().getExecutorManager().getExecutorFactory();
        }
        return null;
    }

    public NDocExecutorManager setExecutorFactory(NDocExecutorFactory processorFactory) {
        this.executorFactory = processorFactory;
        return this;
    }

    public String executeRegularFile(Path path, String mimeTypesString) {
        Path absolutePath = context.toAbsolutePath(path);
        Path parentPath = absolutePath.getParent();
        if (!Files.isRegularFile(absolutePath)) {
            throw new NIllegalArgumentException(NMsg.ofC("no a file : %s", path));
        }
        NDocExecutor proc = getExecutor(path, mimeTypesString).get();

        try {
            String s1 = path.toString();
            String s2 = absolutePath.toString();
            if (s1.equals(s2)) {
                context.getLog().debug("file", "[" + proc + "] [" + mimeTypesString + "] execute path : " + s1);
            } else {
                context.getLog().debug("file", "[" + proc + "] [" + mimeTypesString + "] execute path : " + s1 + " = " + s2);
            }
            try (InputStream in = Files.newInputStream(absolutePath)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                proc.processStream(in, out,
                        context.newChild()
                                .setUserParentProperties(true)
                                .setWorkingDir(parentPath.toString())
                                .setSourcePath(absolutePath.toString())
                                .setVar("source", absolutePath.toString())
                                .setVar("dir", parentPath.toString())
                                .setVar("cwd", System.getProperty("user.dir"))
                );
                return out.toString();
            }
        } catch (IOException ex) {
            throw new NIOException(NMsg.ofC("error executing file : %s", path), ex);
        }
    }

}
