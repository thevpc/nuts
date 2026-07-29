package net.thevpc.nuts.platform;

import net.thevpc.nuts.util.NOptional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NGpu {
    private final String name;
    private final NRam vram;
    private final Map<String, String> capabilities;

    public NGpu(String name, NRam vram, Map<String, String> capabilities) {
        this.name = name;
        this.vram = vram;
        this.capabilities = capabilities==null?new HashMap<>():new HashMap<>(capabilities);
    }

    public String name() {
        return name;
    }

    public NRam vram() {
        return vram;
    }

    public NOptional<String> capability(String key) {
        return NOptional.ofNamed(capabilities.get(key), key);
    }

    public Map<String, String> capabilities() {
        return Collections.unmodifiableMap(capabilities);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NGpu nGpu = (NGpu) o;
        return Objects.equals(name, nGpu.name) && Objects.equals(vram, nGpu.vram) && Objects.equals(capabilities, nGpu.capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, vram, capabilities);
    }
}
