package net.thevpc.nuts.lib.doc.processor;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.context.DefaultNDocPathTranslator;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.mimetype.MimeTypeConstants;
import net.thevpc.nuts.lib.doc.processor.base.TagStreamProcessor;
import net.thevpc.nuts.lib.doc.processor.impl.*;
import net.thevpc.nuts.lib.doc.util.FileProcessorUtils;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class NDocProcessorManager {
    private static final Map<String, NDocProcessor> globalProcessorsByMimeType = new HashMap<>();
    public static final StreamToProcessor DEFAULT_PROCESSOR = new StreamToProcessor(new CopyStreamProcessor());

    static {
        registerGlobalProcessorByMimeType(TagStreamProcessor.DOLLAR, MimeTypeConstants.PLACEHOLDER_DOLLAR);
        registerGlobalProcessorByMimeType(TagStreamProcessor.BARACKET2, MimeTypeConstants.PLACEHOLDER_BRACKET2);
        registerGlobalProcessorByMimeType(TagStreamProcessor.DOLLAR_BARACKET2, MimeTypeConstants.PLACEHOLDER_DOLLAR_BRACKET2);
        registerGlobalProcessorByMimeType(TagStreamProcessor.LT_PERCENT, MimeTypeConstants.PLACEHOLDER_LT_PERCENT);

        registerGlobalProcessorByMimeType(TagStreamProcessor.BARACKET2, "text/html","text/markdown","text/x-shellscript", "application/x-shellscript");
        for (String mimeType : NConstants.Ntf.MIME_TYPES) {
            registerGlobalProcessorByMimeType(TagStreamProcessor.BARACKET2, mimeType);
        }
        registerGlobalProcessorByMimeType(TagStreamProcessor.DOLLAR_BARACKET2,
                MimeTypeConstants.ANY_TEXT,
                "application/json",
                "application/javascript",
                "application/java"
        );
        globalProcessorsByMimeType.put(MimeTypeConstants.ANY_TYPE, DEFAULT_PROCESSOR);
    }

    private NDocContext context;
    private final Map<String, NDocProcessor> processorsByMimeType = new HashMap<>();
    private NDocProcessorFactory processorFactory;

    public static void registerGlobalProcessorByMimeType(NDocProcessor p, String... mimetypes) {
        for (String mimetype : mimetypes) {
            globalProcessorsByMimeType.put(mimetype, p);
        }
    }

    public static void registerGlobalProcessorByMimeType(NDocStreamProcessor p, String... mimetypes) {
        for (String mimetype : mimetypes) {
            globalProcessorsByMimeType.put(mimetype, new StreamToProcessor(p));
        }
    }

    public NDocProcessorManager(NDocContext context) {
        this.context = context;
    }

    public NDocProcessor getProcessorExact(String mimetype) {
        if (processorFactory != null) {
            NDocProcessor p = processorFactory.getProcessor(mimetype);
            if (p != null) {
                return p;
            }
        }
        NDocProcessor p = processorsByMimeType.get(mimetype);
        if (p != null) {
            return p;
        }
        if (context.getParent() != null) {
            return context.getParent().getProcessorManager().getProcessorExact(mimetype);
        }
        p = globalProcessorsByMimeType.get(mimetype);
        if (p != null) {
            return p;
        }
        //never return null!
        return null;//DEFAULT_PROCESSOR;
    }

    public NDocProcessor getProcessor(String mimetype) {
        if (mimetype == null || mimetype.isEmpty() || mimetype.equals("*")) {
            mimetype = MimeTypeConstants.ANY_TYPE;
        }
        for (String mt : mimetype.split(";")) {
            mt = mt.trim();
            if (mt.length() > 0) {
                NDocProcessor m = getProcessorExact(mt);
                if (m != null) {
                    return m;
                }
            }
        }
        for (String mt : mimetype.split(";")) {
            mt = mt.trim();
            if (mt.length() > 0) {
                int slash = mimetype.indexOf('/');
                if (slash > 0) {
                    String a = mimetype.substring(0, slash);
                    String b = mimetype.substring(slash + 1);
                    if (!b.equals("*")) {
                        NDocProcessor m = getProcessorExact(a + "/*");
                        if (m != null) {
                            return m;
                        }
                    }
                }
            }
        }
        return getProcessorExact(MimeTypeConstants.ANY_TYPE);
    }


    public NDocProcessor getDefaultProcessor(String mimetype) {
        NDocProcessor processor = processorsByMimeType.get(mimetype);
        if (processor != null) {
            return processor;
        }
        if (context.getParent() != null) {
            return context.getParent().getProcessorManager().getDefaultProcessor(mimetype);
        }
        return null;
    }


    public NDocProcessorManager setDefaultProcessor(String mimetype, NDocProcessor processor) {
        if (processor == null) {
            processorsByMimeType.remove(mimetype);
        } else {
            processorsByMimeType.put(mimetype, processor);
        }
        return this;
    }


    public NDocProcessorFactory getProcessorFactory() {
        if (processorFactory != null) {
            return processorFactory;
        }
        if (context.getParent() != null) {
            return context.getParent().getProcessorManager().getProcessorFactory();
        }
        return null;
    }

    public NDocProcessorManager setProcessorFactory(NDocProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
        return this;
    }

    public void processResourceTree(NPath path, Predicate<NPath> filter) {
        if (!context.getRootDir().isPresent()) {
            context.setRootDir(path.toString());
        }
        if (!path.exists()) {
            context.getLog().warn("file", NMsg.ofC("source file not found %s", path).toString());
            return;
        }
        NPath path0 = context.toAbsolutePath(path);
        if (path0.isRegularFile()) {
            if (filter == null || filter.test(path0)) {
                getProcessorExact(MimeTypeConstants.ANY_TYPE).processPath(path, null, context);
            }
        } else if (path.isDirectory()) {
            try (NStream<NPath> stream = path0.walk().filter(x -> x.isRegularFile())) {
                stream.forEach(x -> {
                    if (filter == null || filter.test(x)) {
                        try {
                            String p = context.getPathTranslator().translatePath(x.toString());
                            if (p != null) {
                                NCp.of().from(x).to(NPath.of(p)).setMkdirs(true).run();
                                //getProcessorExact(MimeTypeConstants.ANY_TYPE).processPath(x, null, context);
                            }
                        } catch (Exception ex) {
                            throw new NIllegalArgumentException(NMsg.ofC("error processing %s : %s", x, ex), ex);
                        }
                    }
                });
            }
        } else {
            throw new NIOException(NMsg.ofC("unsupported path %s", path));
        }
    }

    public void processSourceRegularFile(NPath path, String mimeType) {
        NPath absolutePath = context.toAbsolutePath(path);
        NPath parentPath = absolutePath.getParent();
        if (!absolutePath.isRegularFile()) {
            throw new NIllegalArgumentException(NMsg.ofC("unsupported file : %s", path.toString()));
        }
        String[] mimeTypes = mimeType == null ? FileProcessorUtils.splitMimeTypes(context.getMimeTypeResolver().resolveMimetype(path.toString()))
                : FileProcessorUtils.splitMimeTypes(mimeType);
        String contextName1 = NStringUtils.firstNonBlank(context.getContextName(), "file");
        for (String mimeType0 : mimeTypes) {
            NDocProcessor proc = null;
            try {
                proc = getProcessor(mimeType0);
            } catch (Exception ex) {
                context.getLog().error(contextName1, "error resolving processor for mimetype " + mimeType0 + " and file : " + path.toString() + ". " + ex.toString());
            }
            if (proc != null) {
                String s1 = path.toString();
                String s2 = absolutePath.toString();
                if (s1.equals(s2)) {
                    context.getLog().debug(contextName1, "[" + proc + "] [" + NStringUtils.firstNonBlank(mimeType, "no-mimetype") + "] process path : " + s1);
                } else {
                    context.getLog().debug(contextName1, "[" + proc + "] [" + NStringUtils.firstNonBlank(mimeType, "no-mimetype") + "] process path : " + s1 + " = " + s2);
                }
                proc.processPath(path, mimeType0,
                        context.newChild()
                                .setUserParentProperties(true)
                                .setWorkingDir(parentPath.toString())
                                .setSourcePath(absolutePath.toString())
                );
                return;
            }
        }
    }
    public void processSourceRegularFile(NPath path, String mimeType,OutputStream out) {
        NPath absolutePath = context.toAbsolutePath(path);
        NPath parentPath = absolutePath.getParent();
        if (!absolutePath.isRegularFile()) {
            throw new NIllegalArgumentException(NMsg.ofC("unsupported file : %s", path.toString()));
        }
        String[] mimeTypes = mimeType == null ? FileProcessorUtils.splitMimeTypes(context.getMimeTypeResolver().resolveMimetype(path.toString()))
                : FileProcessorUtils.splitMimeTypes(mimeType);
        String contextName1 = NStringUtils.firstNonBlank(context.getContextName(), "file");
        for (String mimeType0 : mimeTypes) {
            NDocProcessor proc = null;
            try {
                proc = getProcessor(mimeType0);
            } catch (Exception ex) {
                context.getLog().error(contextName1, "error resolving processor for mimetype " + mimeType0 + " and file : " + path.toString() + ". " + ex.toString());
            }
            if (proc != null) {
                String s1 = path.toString();
                String s2 = absolutePath.toString();
                if (s1.equals(s2)) {
                    context.getLog().debug(contextName1, "[" + proc + "] [" + NStringUtils.firstNonBlank(mimeType, "no-mimetype") + "] process path : " + s1);
                } else {
                    context.getLog().debug(contextName1, "[" + proc + "] [" + NStringUtils.firstNonBlank(mimeType, "no-mimetype") + "] process path : " + s1 + " = " + s2);
                }
                try(InputStream in=path.getInputStream()) {
                    proc.processStream(in, out,
                            context.newChild()
                                    .setUserParentProperties(true)
                                    .setWorkingDir(parentPath.toString())
                                    .setSourcePath(absolutePath.toString())
                    );
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                return;
            }
        }
    }

    public void processSourceTree(NPath path, String targetFolder, Predicate<NPath> filter) {
        NPath opath = path.normalize();
        if (!opath.exists()) {
            context.getLog().warn("file", NMsg.ofC("source file not found %s", opath).toString());
            return;
        } else if (opath.isDirectory()) {
            context.setWorkingDir(opath.toString());
            if (targetFolder != null) {
                context.setPathTranslator(new DefaultNDocPathTranslator(opath, NPath.of(targetFolder)));
            }
        } else {
            NPath ppath = opath.getParent();
            context.setWorkingDir(ppath.toString());
            if (targetFolder != null) {
                context.setPathTranslator(new DefaultNDocPathTranslator(ppath,NPath.of(targetFolder) ));
            }
        }
        processSourceTree(opath, filter);
    }

    public void processSourceTree(NPath path, Predicate<NPath> filter) {
        if (!context.getRootDir().isPresent()) {
            context.setRootDir(path.toString());
        }
        NPath path0 = context.toAbsolutePath(path);
        if (!path0.exists()) {
            context.getLog().warn("file", NMsg.ofC("source file not found %s", path0).toString());
            return;
        } else if (path0.isRegularFile()) {
            if (filter == null || filter.test(path0)) {
                processSourceRegularFile(path, null);
            }
        } else if (path.isDirectory()) {
            try (NStream<NPath> stream = path0.walk().filter(x -> x.isRegularFile())) {
                stream.forEach(x -> {
                    if (filter == null || filter.test(x)) {
                        try {
                            processSourceRegularFile(x, null);
                        } catch (Exception ex) {
                            throw new NIllegalArgumentException(NMsg.ofC("error processing %s : %s", x, ex), ex);
                        }
                    }
                });
            }
        } else {
            throw new NIOException(NMsg.ofC("unsupported path %s", path));
        }
    }

    public void processResourceTree(NPath path, String targetFolder, Predicate<NPath> filter) {
        NPath opath = path.normalize();
        if (!opath.exists()) {
            context.getLog().warn("file", NMsg.ofC("source file not found %s", opath).toString());
            return;
        } else if (opath.isDirectory()) {
            context.setWorkingDir(opath.toString());
            if (targetFolder != null) {
                context.setPathTranslator(new DefaultNDocPathTranslator(opath, NPath.of(targetFolder)));
            }
        } else {
            NPath ppath = opath.getParent();
            context.setWorkingDir(ppath.toString());
            if (targetFolder != null) {
                context.setPathTranslator(new DefaultNDocPathTranslator(opath, ppath));
            }
        }
        processResourceTree(opath, filter);
    }

    public void processFiles(NPath path, Predicate<NPath> filter) {
        NPath path0 = context.toAbsolutePath(path);
        if (!path0.exists()) {
            context.getLog().warn("file", NMsg.ofC("source file not found %s", path0).toString());
            return;
        } else if (path0.isRegularFile()) {
            if (filter == null || filter.test(path0)) {
                processSourceRegularFile(path0, null);
            }
        } else if (path0.isDirectory()) {
            try (Stream<NPath> stream = path0.list().stream()) {
                stream.filter(filter)
                        .forEach(x -> {
                            if (x.isRegularFile()) {
                                if (filter == null || filter.test(x)) {
                                    processSourceRegularFile(path0, null);
                                }
                            }
                        });
            }
        } else {
            throw new NIOException(NMsg.ofC("unsupported path %s", path));
        }
    }

    public NOptional<NDocProcessor> resolveFileProcessor(NPath path, String mimeType) {
        NPath absolutePath = context.toAbsolutePath(path);
        NPath parentPath = absolutePath.getParent();
        if (!absolutePath.isRegularFile()) {
            return NOptional.ofNamedEmpty(NMsg.ofC("processor not found for %s and %s", path, mimeType));
        }
        String[] mimeTypes = mimeType == null ? FileProcessorUtils.splitMimeTypes(context.getMimeTypeResolver().resolveMimetype(path.toString()))
                : FileProcessorUtils.splitMimeTypes(mimeType);
        String contextName1 = NStringUtils.firstNonBlank(context.getContextName(), "file");
        for (String mimeType0 : mimeTypes) {
            NDocProcessor proc = null;
            try {
                proc = getProcessor(mimeType0);
            } catch (Exception ex) {
                context.getLog().error(contextName1, "error resolving processor for mimetype " + mimeType0 + " and file : " + path.toString() + ". " + ex.toString());
            }
            if (proc != null) {
                String s1 = path.toString();
                String s2 = absolutePath.toString();
                if (s1.equals(s2)) {
                    context.getLog().debug(contextName1, "[" + proc + "] [" + NStringUtils.firstNonBlank(mimeType, "no-mimetype") + "] process path : " + s1);
                } else {
                    context.getLog().debug(contextName1, "[" + proc + "] [" + NStringUtils.firstNonBlank(mimeType, "no-mimetype") + "] process path : " + s1 + " = " + s2);
                }
                return NOptional.of(proc);
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("processor not found for %s and %s", path, mimeType));
    }

    public String processString(String source, String mimeType) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        processStream(new ByteArrayInputStream(source.getBytes()), bos, mimeType);
        return bos.toString();
    }

    public void processStream(InputStream source, OutputStream target, String mimeType) {
        String[] mimeTypes = FileProcessorUtils.splitMimeTypes(mimeType);
        for (String mimeType0 : mimeTypes) {
            NDocProcessor proc = null;
            try {
                proc = getProcessor(mimeType0);
            } catch (Exception ex) {
                context.getLog().error("file", "unsupported mimeType : " + mimeType0 + ". " + ex);
            }
            if (proc != null) {
                try {
                    proc.processStream(source, target,
                            context.newChild()
                                    .setUserParentProperties(true)
                    );
                } catch (Exception ex) {
                    context.getLog().error("file", "error processing mimeType : " + mimeType + ". " + ex);
                }
                return;
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported mimetype : %s", mimeType));
    }

}
