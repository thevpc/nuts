package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.platform.NGpuDevice;
import net.thevpc.nuts.platform.NGpuDeviceType;
import net.thevpc.nuts.platform.NGpuVendor;
import net.thevpc.nuts.platform.NRam;
import net.thevpc.nuts.util.NOptional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@link NGpuDevice}.
 *
 * @author thevpc
 * @since 1.0.0
 */
public class DefaultNGpuDevice implements NGpuDevice {

    private static final String[] TURING_WITHOUT_TENSOR_CORES_EXACT = {
            "NVIDIA GeForce MX450",
            "NVIDIA GeForce MX550"
    };

    private static final String TURING_WITHOUT_TENSOR_CORES_PREFIX = "NVIDIA GeForce GTX 16";

    private final String name;
    private final NRam vram;
    private final Map<String, String> capabilities;

    public DefaultNGpuDevice(String name, NRam vram, Map<String, String> capabilities) {
        this.name = name;
        this.vram = vram;
        this.capabilities = capabilities == null ? new HashMap<>() : new HashMap<>(capabilities);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public NRam vram() {
        return vram;
    }

    @Override
    public NOptional<String> capability(String key) {
        return NOptional.ofNamed(capabilities.get(key), key);
    }

    @Override
    public Map<String, String> capabilities() {
        return Collections.unmodifiableMap(capabilities);
    }

    @Override
    public NGpuVendor vendor() {
        return capability(VENDOR).flatMap(NGpuVendor::parse).orElse(NGpuVendor.UNKNOWN);
    }

    @Override
    public NGpuDeviceType deviceType() {
        return capability(DEVICE_TYPE).flatMap(NGpuDeviceType::parse).orElse(NGpuDeviceType.UNKNOWN);
    }

    @Override
    public int computeCapability() {
        String value = capability(COMPUTE_CAPABILITY).orNull();
        if (value == null) {
            return -1;
        }
        String[] parts = value.trim().split("\\.");
        if (parts.length < 2) {
            return -1;
        }
        try {
            return Integer.parseInt(parts[0].trim()) * 100 + Integer.parseInt(parts[1].trim()) * 10;
        } catch (Exception ignored) {
            return -1;
        }
    }

    @Override
    public boolean isComputeCapable() {
        String expected = vendor().getComputeKernelModule();
        return expected != null && expected.equals(capability(KERNEL_DRIVER).orNull());
    }

    @Override
    public boolean hasTensorCores() {
        if (vendor() != NGpuVendor.NVIDIA || computeCapability() < CC_TURING) {
            return false;
        }
        if (name != null) {
            for (String s : TURING_WITHOUT_TENSOR_CORES_EXACT) {
                if (s.equals(name)) {
                    return false;
                }
            }
            if (name.startsWith(TURING_WITHOUT_TENSOR_CORES_PREFIX)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSupportedFp16() {
        return vendor() == NGpuVendor.NVIDIA && computeCapability() >= CC_PASCAL;
    }

    @Override
    public boolean isSupportedBf16() {
        return vendor() == NGpuVendor.NVIDIA && computeCapability() >= CC_AMPERE;
    }

    @Override
    public boolean isSupportedInt8() {
        return vendor() == NGpuVendor.NVIDIA && computeCapability() >= CC_DP4A;
    }

    @Override
    public boolean isSupportedInt4() {
        return hasTensorCores();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNGpuDevice that = (DefaultNGpuDevice) o;
        return Objects.equals(name, that.name) && Objects.equals(vram, that.vram) && Objects.equals(capabilities, that.capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, vram, capabilities);
    }

    public static List<NGpuDevice> orderWithPrimaryFirst(List<NGpuDevice> gpus) {
        if (gpus == null || gpus.isEmpty()) {
            return Collections.emptyList();
        }
        NOptional<NGpuDevice> p = NGpuDevice.primary(gpus);
        if (!p.isPresent()) {
            return Collections.unmodifiableList(gpus);
        }
        NGpuDevice primary = p.get();
        List<NGpuDevice> reordered = new java.util.ArrayList<>(gpus.size());
        reordered.add(primary);
        for (NGpuDevice g : gpus) {
            if (g != null && !g.equals(primary)) {
                reordered.add(g);
            }
        }
        return Collections.unmodifiableList(reordered);
    }

    @Override
    public String toString() {
        return "NGpuDevice{" +
                "name='" + name + '\'' +
                ", vram=" + vram +
                ", capabilities=" + capabilities +
                '}';
    }
}
