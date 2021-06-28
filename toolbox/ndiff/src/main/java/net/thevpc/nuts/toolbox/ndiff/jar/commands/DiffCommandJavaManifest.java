package net.thevpc.nuts.toolbox.ndiff.jar.commands;

import net.thevpc.nuts.toolbox.ndiff.jar.*;
import net.thevpc.nuts.toolbox.ndiff.jar.util.DiffUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class DiffCommandJavaManifest extends AbstractDiffCommand {
    public static final DiffCommandJavaManifest INSTANCE = new DiffCommandJavaManifest();

    protected DiffCommandJavaManifest() {
        super("java-manifest");
    }

    @Override
    public int acceptInput(Object input) {
        if (input instanceof Manifest) {
            return 100;
        }
        if (input instanceof InputStream) {
            return 1;
        }
        if (input instanceof byte[]) {
            return 1;
        }
        if (input instanceof File) {
            File f = ((File) input);
            String n = f.getName().toLowerCase();
            if (n.endsWith(".mf")) {
                return 100;
            }
        }
        return -1;
    }

    public Manifest prepareSourceOrTarget(Object source) {
        if (source instanceof File) {
            try {
                try (FileInputStream i = new FileInputStream((File) source)) {
                    return new Manifest(i);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        if (source instanceof Manifest) {
            return (Manifest) source;
        }
        if (source instanceof InputStream) {
            try {
                return new Manifest((InputStream) source);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        if (source instanceof byte[]) {
            try {
                return new Manifest(new ByteArrayInputStream((byte[]) source));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        throw new IllegalArgumentException("Unsupported Manifest Input " + source);
    }

    @Override
    public boolean acceptDiffKey(DiffKey key) {
        switch (key.getKind()) {
            case DiffKey.KIND_FILE: {
                return DiffUtils.isFileName("MANIFEST.MF", key.getName());
            }
        }
        return false;
    }

    public Map<DiffKey, String> map(Object item, DiffEvalContext diffEvalContext) {
        Manifest m = prepareSourceOrTarget(item);
        HashMap<DiffKey, String> s = new HashMap<>();
        if (m != null) {
            for (Map.Entry<Object, Object> e : m.getMainAttributes().entrySet()) {
                s.put(new DiffKey(e.getKey().toString(), DiffKey.KIND_VAR, 0), String.valueOf(e.getValue()));
            }
            for (Map.Entry<String, Attributes> uu : m.getEntries().entrySet()) {
                for (Map.Entry<Object, Object> e : uu.getValue().entrySet()) {
                    s.put(new DiffKey(uu.getKey() + "." + e.getKey().toString(), DiffKey.KIND_VAR, 0), String.valueOf(e.getValue()));
                }
            }
        }
        return s;
    }

    @Override
    public String hash(InputStream source) {
        Manifest m = null;
        try {
            m = DiffUtils.prepareManifest(new Manifest(source));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            m.write(os);
            return DiffUtils.hashStream(new ByteArrayInputStream(os.toByteArray()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public DiffItem createContentDiffItem(DiffItemCreateContext context) {
        switch (context.getStatus()) {
            case ADDED: {
                return new DefaultDiffItem("java-manifest", context.getKey().getName(), DiffStatus.ADDED, null, null);
            }
            case REMOVED: {
                return new DefaultDiffItem("java-manifest", context.getKey().getName(), DiffStatus.REMOVED, null, null);
            }
            case CHANGED: {
                List<DiffItem> details = null;
                if (context.getEvalContext().isVerbose()) {
                    DiffCommandZip.ZipSource zsrc = (DiffCommandZip.ZipSource) context.getEvalContext().getSource();
                    DiffCommandZip.ZipSource ztgt = (DiffCommandZip.ZipSource) context.getEvalContext().getTarget();
                    try (InputStream i1 = zsrc.getInputStream(context.getKey().getName())) {
                        try (InputStream i2 = ztgt.getInputStream(context.getKey().getName())) {
                            DefaultDiffEvalContext evalContext = new DefaultDiffEvalContext(context.getEvalContext());
                            evalContext.setSource(i1);
                            evalContext.setTarget(i2);
                            details = eval(evalContext).all();
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                return new DefaultDiffItem("java-manifest", context.getKey().getName(), DiffStatus.CHANGED, null/*sourceValue+" -> "+targetValue*/, details);
            }
        }
        throw new UnsupportedOperationException();
    }

}
