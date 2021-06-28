package net.thevpc.nuts.toolbox.ndiff.jar.commands;

import net.thevpc.nuts.toolbox.ndiff.jar.*;
import net.thevpc.nuts.toolbox.ndiff.jar.util.DiffUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class DiffCommandJavaClass extends AbstractDiffCommand {
    public static final DiffCommand INSTANCE=new DiffCommandJavaClass();

    protected DiffCommandJavaClass() {
        super("java-class");
    }
    @Override
    public int acceptInput(Object input) {
        if (input instanceof InputStream) {
            return 1;
        }
        if (input instanceof byte[]) {
            return 1;
        }
        if (input instanceof File) {
            File f = ((File) input);
            String n = f.getName().toLowerCase();
            if (n.endsWith(".class")) {
                return 100;
            }
        }
        return -1;
    }

    @Override
    public Object prepareSourceOrTarget(Object source) {
        if (source instanceof File) {
            try {
                return Files.readAllBytes(((File)source).toPath());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        if (source instanceof InputStream) {
            DiffUtils.allBytes((InputStream) source);
        }
        if (source instanceof byte[]) {
            return source;
        }
        throw new IllegalArgumentException("Unsupported Java class Input " + source);
    }

    @Override
    public boolean acceptDiffKey(DiffKey key) {
        switch (key.getKind()) {
            case DiffKey.KIND_FILE: {
                return key.getName().endsWith(".class");
            }
        }
        return false;
    }

    @Override
    public Map<DiffKey, String> map(Object item, DiffEvalContext diffEvalContext) {
        return new HashMap<>();
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

}
