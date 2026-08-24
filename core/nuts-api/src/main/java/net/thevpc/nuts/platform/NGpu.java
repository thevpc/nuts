package net.thevpc.nuts.platform;

import net.thevpc.nuts.util.NOptional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * NGpu class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NGpu {
    private final String name;
    private final NRam vram;
    private final Map<String, String> capabilities;

    /**
     * N gpu.
     *
     * @param name name
     * @param vram vram
     * @param capabilities capabilities
     * @return n gpu result
     */
    public NGpu(String name, NRam vram, Map<String, String> capabilities) {
        this.name = name;
        this.vram = vram;
        this.capabilities = capabilities==null?new HashMap<>():new HashMap<>(capabilities);
    }

    /**
     * Name.
     *
     * @return name result
     */
    public String name() {
        return name;
    }

    /**
     * Vram.
     *
     * @return vram result
     */
    public NRam vram() {
        return vram;
    }

    /**
     * Capability.
     *
     * @param key key
     * @return capability result
     */
    public NOptional<String> capability(String key) {
        return NOptional.ofNamed(capabilities.get(key), key);
    }

    /**
     * Capabilities.
     *
     * @return capabilities result
     */
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
