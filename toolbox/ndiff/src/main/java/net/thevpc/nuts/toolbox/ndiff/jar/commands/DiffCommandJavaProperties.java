package net.thevpc.nuts.toolbox.ndiff.jar.commands;

import net.thevpc.nuts.toolbox.ndiff.jar.*;
import net.thevpc.nuts.toolbox.ndiff.jar.util.DiffUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DiffCommandJavaProperties extends AbstractDiffCommand {
    public static final DiffCommandJavaProperties INSTANCE = new DiffCommandJavaProperties();

    protected DiffCommandJavaProperties() {
        super("java-properties");
    }

    @Override
    public int acceptInput(Object input) {
        if (input instanceof Properties) {
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
            if (n.endsWith(".properties")) {
                return 100;
            }
            if (n.endsWith(".props")) {
                return 50;
            }
            if (n.endsWith(".config")) {
                return 40;
            }
        }
        return -1;
    }

    @Override
    public boolean acceptDiffKey(DiffKey key) {
        switch (key.getKind()) {
            case DiffKey.KIND_FILE: {
                return key.getName().endsWith("properties");
            }
        }
        return false;
    }

    public Map<DiffKey, String> map(Object item, DiffEvalContext diffEvalContext) {
        Properties m = null;
        if (item != null) {
            if (item instanceof Properties) {
                m = (Properties) item;
            } else if (item instanceof InputStream) {
                m = new Properties();
                try {
                    m.load((InputStream) item);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            } else {
                throw new IllegalArgumentException("Unsupported properties item");
            }
        }
        HashMap<DiffKey, String> s = new HashMap<>();
        if (m != null) {
            for (Map.Entry<Object, Object> e : m.entrySet()) {
                s.put(new DiffKey(e.getKey().toString(), DiffKey.KIND_VAR, 0), String.valueOf(e.getValue()));
            }
        }
        return s;
    }

    @Override
    public String hash(InputStream source) {
        Properties p = new Properties();
        try {
            p.load(source);
            try {
                byte[] buffer = new byte[8192];
                MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                TreeSet<String> keys = new TreeSet<String>((Set) p.keySet());
                for (String key : keys) {
                    byte[] r = key.getBytes();
                    sha1.update(buffer, 0, r.length);
                    r = p.getProperty(key).getBytes();
                    sha1.update(buffer, 0, r.length);
                }
                return DiffUtils.bytesToHex(sha1.digest());
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public DiffItem createContentDiffItem(DiffItemCreateContext context) {
        switch (context.getStatus()) {
            case ADDED: {
                return new DefaultDiffItem("java-properties", context.getKey().getName(), DiffStatus.ADDED, null, null);
            }
            case REMOVED: {
                return new DefaultDiffItem("java-properties", context.getKey().getName(), DiffStatus.REMOVED, null, null);
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
                return new DefaultDiffItem("java-properties", context.getKey().getName(), DiffStatus.CHANGED, null/*sourceValue+" -> "+targetValue*/, details);
            }
        }
        throw new UnsupportedOperationException();
    }
}
