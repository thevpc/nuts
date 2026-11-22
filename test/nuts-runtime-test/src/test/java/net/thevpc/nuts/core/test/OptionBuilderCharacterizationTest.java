package net.thevpc.nuts.core.test;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNBootOptionsBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNWorkspaceOptionsBuilder;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ARCH-08 characterization lock for the two option builders. Every public
 * zero-arg getter and every supported one-arg setter is exercised reflectively
 * so that extracting field groups cannot silently change builder state,
 * defaults, copy semantics or the resulting options object.
 */
public class OptionBuilderCharacterizationTest {

    @BeforeAll
    static void initWorkspace() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void publicApiSurfaceIsStable() throws Exception {
        StringBuilder sb = new StringBuilder("API\n");
        appendApi(sb, DefaultNBootOptionsBuilder.class);
        appendApi(sb, DefaultNWorkspaceOptionsBuilder.class);
        assertBaseline("option-builder-api", sb.toString());
    }

    @Test
    public void bootOptionsBuilderStateIsStable() throws Exception {
        DefaultNBootOptionsBuilder b = new DefaultNBootOptionsBuilder();
        applySupportedSetters(b);
        StringBuilder sb = new StringBuilder("BUILDER\n").append(snapshot(b));
        sb.append("BUILT\n").append(snapshot(b.build()));
        sb.append("COPY\n").append(snapshot(b.copy()));
        DefaultNBootOptionsBuilder copiedFrom = new DefaultNBootOptionsBuilder();
        copiedFrom.copyFrom(b);
        sb.append("COPY_FROM\n").append(snapshot(copiedFrom));
        assertBaseline("boot-options-builder", sb.toString());
    }

    @Test
    public void workspaceOptionsBuilderStateIsStable() throws Exception {
        DefaultNWorkspaceOptionsBuilder b = new DefaultNWorkspaceOptionsBuilder();
        applySupportedSetters(b);
        StringBuilder sb = new StringBuilder("BUILDER\n").append(snapshot(b));
        sb.append("COPY\n").append(snapshot(b.copy()));
        DefaultNWorkspaceOptionsBuilder copiedFrom = new DefaultNWorkspaceOptionsBuilder();
        copiedFrom.copyFrom(b);
        sb.append("COPY_FROM\n").append(snapshot(copiedFrom));
        DefaultNWorkspaceOptionsBuilder reset = new DefaultNWorkspaceOptionsBuilder();
        applySupportedSetters(reset);
        reset.resetOptions();
        sb.append("RESET\n").append(snapshot(reset));
        assertBaseline("workspace-options-builder", sb.toString());
    }

    private static void appendApi(StringBuilder sb, Class<?> type) {
        sb.append(type.getName()).append('\n');
        for (Constructor<?> c : type.getDeclaredConstructors()) {
            if (Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers())) {
                sb.append("CTOR(").append(paramTypes(c.getParameterTypes())).append(")\n");
            }
        }
        List<String> methods = new ArrayList<>();
        for (Method m : type.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if ((Modifier.isPublic(mod) || Modifier.isProtected(mod)) && !m.isSynthetic()) {
                methods.add("METHOD " + m.getName() + "(" + paramTypes(m.getParameterTypes())
                        + "): " + m.getReturnType().getCanonicalName());
            }
        }
        Collections.sort(methods);
        methods.forEach(sb::append);
        methods.forEach(m -> sb.append('\n'));
    }

    private static String paramTypes(Class<?>[] types) {
        List<String> ret = new ArrayList<>();
        for (Class<?> t : types) {
            ret.add(t.getCanonicalName());
        }
        return String.join(",", ret);
    }

    private static void assertBaseline(String name, String canonical) throws Exception {
        String hash = sha256(canonical);
        Path baseline = Paths.get("src/test/resources/arch08-" + name + ".txt")
                .toAbsolutePath().normalize();
        if (System.getProperty("nuts.builder.generate") != null) {
            Files.createDirectories(baseline.getParent());
            Files.write(baseline, (hash + "\n").getBytes(StandardCharsets.UTF_8));
            return;
        }
        Assertions.assertTrue(Files.exists(baseline), "missing baseline: " + baseline);
        Assertions.assertEquals(new String(Files.readAllBytes(baseline), StandardCharsets.UTF_8).trim(),
                hash, name + " observable state changed");
    }

    private static void applySupportedSetters(Object target) throws Exception {
        List<Method> setters = new ArrayList<>();
        for (Method m : target.getClass().getMethods()) {
            if (m.getParameterCount() != 1
                    || !Modifier.isPublic(m.getModifiers())
                    || m.getDeclaringClass() == Object.class
                    || m.getName().startsWith("copyFrom")
                    || m.getName().startsWith("set")
                    || m.getParameterTypes()[0].isAssignableFrom(target.getClass())) {
                continue;
            }
            String getter = m.getName();
            try {
                target.getClass().getMethod(getter);
            } catch (NoSuchMethodException ex) {
                continue;
            }
            if (sampleValue(m.getParameterTypes()[0]) != null) {
                setters.add(m);
            }
        }
        setters.sort(Comparator.comparing(Method::getName));
        for (Method m : setters) {
            Object value = sampleValue(m.getParameterTypes()[0]);
            try {
                m.invoke(target, value);
            } catch (Exception ex) {
                Assertions.fail("setter " + m.getName() + " rejected sample value " + value + ": " + ex);
            }
        }
    }

    private static String snapshot(Object target) throws Exception {
        List<Method> getters = new ArrayList<>();
        for (Method m : target.getClass().getMethods()) {
            if (m.getParameterCount() == 0 && Modifier.isPublic(m.getModifiers())
                    && NOptional.class.isAssignableFrom(m.getReturnType())) {
                getters.add(m);
            }
        }
        getters.sort(Comparator.comparing(Method::getName));
        StringBuilder sb = new StringBuilder();
        for (Method m : getters) {
            Object value = m.invoke(target);
            Object raw = value instanceof NOptional ? ((NOptional<?>) value).orNull() : value;
            sb.append(m.getName()).append('=').append(render(raw)).append('\n');
        }
        return sb.toString();
    }

    private static Object sampleValue(Class<?> type) {
        if (type == String.class) {
            return "sample";
        }
        if (type == Boolean.class || type == boolean.class) {
            return Boolean.TRUE;
        }
        if (type == Integer.class || type == int.class) {
            return 42;
        }
        if (type == Long.class || type == long.class) {
            return 42L;
        }
        if (type == Instant.class) {
            return Instant.parse("2020-01-02T03:04:05Z");
        }
        if (type == char[].class) {
            return "sample-secret".toCharArray();
        }
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants == null || constants.length == 0 ? null : constants[0];
        }
        if (type == NVersion.class) {
            return NVersion.get("1.2.0").orNull();
        }
        if (type == NId.class) {
            return NId.of("net.thevpc:sample#1.2.0");
        }
        if (type == NMsg.class) {
            return NMsg.ofC("sample message");
        }
        if (type == List.class) {
            return Arrays.asList("alpha", "beta");
        }
        if (type == Map.class) {
            Map<Object, Object> m = new HashMap<>();
            m.put("key", "value");
            return m;
        }
        if (type.isArray() && type.getComponentType() == String.class) {
            return new String[]{"alpha", "beta"};
        }
        return null;
    }

    private static String render(Object value) {
        if (value == null) {
            return "<null>";
        }
        if (value instanceof char[]) {
            return "char[]" + Arrays.toString((char[]) value);
        }
        if (value.getClass().isArray()) {
            int n = Array.getLength(value);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < n; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(render(Array.get(value, i)));
            }
            return sb.append(']').toString();
        }
        if (value instanceof Map) {
            Map<String, String> sorted = new TreeMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
                sorted.put(String.valueOf(e.getKey()), render(e.getValue()));
            }
            return sorted.toString();
        }
        if (value instanceof List) {
            List<String> ret = new ArrayList<>();
            for (Object o : (List<?>) value) {
                ret.add(render(o));
            }
            return ret.toString();
        }
        return String.valueOf(value);
    }

    private static String sha256(String s) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
