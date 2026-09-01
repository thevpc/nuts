package net.thevpc.nuts.platform;

import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;

/**
 * Represents a GPU hardware device with name, VRAM memory, and capability attributes.
 *
 * @author thevpc
 * @app.category Base
 * @since 1.0.0
 */
public interface NGpuDevice {

    /**
     * Pci address, as an example {@code 0000:01:00.0}. Stable identity of a
     * device, and the key to use when merging readings from several sources.
     */
    String PCI_BUS_ID = "pci.bus.id";

    /**
     * Raw pci device id, as an example {@code 0x28e1}.
     */
    String PCI_DEVICE_ID = "pci.device.id";

    /**
     * Hardware vendor, holding an {@link NGpuVendor} id such as {@code nvidia}.
     */
    String VENDOR = "vendor";

    /**
     * Device kind, holding an {@link NGpuDeviceType} id such as
     * {@code dedicated-gpu}.
     */
    String DEVICE_TYPE = "device.type";

    /**
     * Kernel module currently bound to the device, as an example
     * {@code nvidia}, {@code nouveau} or {@code i915}.
     */
    String KERNEL_DRIVER = "kernel.driver";

    /**
     * Vendor driver version, as an example {@code 580.159.03}.
     */
    String DRIVER_VERSION = "driver.version";

    /**
     * Vendor assigned device uuid.
     */
    String UUID = "uuid";

    /**
     * Compute capability as reported by the vendor tool, in the
     * {@code major.minor} form such as {@code 8.9}.
     */
    String COMPUTE_CAPABILITY = "compute.capability";

    /**
     * Current pci express link generation, as an example {@code 1}.
     */
    String PCIE_GEN_CURRENT = "pcie.gen.current";

    /**
     * Highest pci express link generation the device supports.
     */
    String PCIE_GEN_MAX = "pcie.gen.max";

    /**
     * Current pci express link width in lanes, as an example {@code 8}.
     */
    String PCIE_WIDTH_CURRENT = "pcie.width.current";

    /**
     * Highest pci express link width the device supports, in lanes.
     */
    String PCIE_WIDTH_MAX = "pcie.width.max";

    /**
     * Theoretical memory bandwidth in gigabytes per second.
     */
    String MEMORY_BANDWIDTH_GBPS = "memory.bandwidth.gbps";

    /**
     * System property forcing the primary gpu device, holding a pci address
     * such as {@code 0000:01:00.0}.
     */
    String PRIMARY_GPU_PROPERTY = "nuts.gpu.device";

    /**
     * Minimum compute capability for fast half precision arithmetic, NVIDIA Pascal.
     */
    int CC_PASCAL = 600;

    /**
     * Minimum compute capability for {@code __dp4a}, integer 8-bit arithmetic.
     */
    int CC_DP4A = 610;

    /**
     * Minimum compute capability for tensor core matrix operations, NVIDIA Turing.
     */
    int CC_TURING = 750;

    /**
     * Minimum compute capability for bfloat16, NVIDIA Ampere.
     */
    int CC_AMPERE = 800;

    /**
     * Name of the GPU device.
     *
     * @return name result
     */
    String name();

    /**
     * VRAM memory information.
     *
     * @return vram result
     */
    NRam vram();

    /**
     * Reads a capability value by key.
     *
     * @param key key
     * @return capability result
     */
    NOptional<String> capability(String key);

    /**
     * Returns all capability attributes.
     *
     * @return capabilities result
     */
    Map<String, String> capabilities();

    /**
     * Hardware vendor of this device.
     *
     * @return the vendor, or {@link NGpuVendor#UNKNOWN} when not reported
     */
    NGpuVendor vendor();

    /**
     * Kind of device (dedicated vs integrated).
     *
     * @return the device type, or {@link NGpuDeviceType#UNKNOWN} when not reported
     */
    NGpuDeviceType deviceType();

    /**
     * Compute capability encoded as {@code major * 100 + minor * 10}.
     *
     * @return encoded compute capability, negative when unknown
     */
    int computeCapability();

    /**
     * Returns true when vendor kernel driver bound permits compute execution.
     *
     * @return true when compute capability is available
     */
    boolean isComputeCapable();

    /**
     * Returns true when tensor cores are available.
     *
     * @return true when tensor cores are available
     */
    boolean hasTensorCores();

    /**
     * Returns true when half precision (fp16) arithmetic is supported.
     *
     * @return true when fp16 is supported
     */
    boolean isSupportedFp16();

    /**
     * Returns true when bfloat16 arithmetic is supported.
     *
     * @return true when bf16 is supported
     */
    boolean isSupportedBf16();

    /**
     * Returns true when integer 8-bit arithmetic is supported.
     *
     * @return true when int8 is supported
     */
    boolean isSupportedInt8();

    /**
     * Returns true when integer 4-bit arithmetic is supported.
     *
     * @return true when int4 is supported
     */
    boolean isSupportedInt4();

    /**
     * Picks the primary GPU device from a list of candidates.
     *
     * @param gpus list of devices to choose from
     * @return the primary device, empty when none is eligible
     */
    static NOptional<NGpuDevice> primary(List<NGpuDevice> gpus) {
        try {
            return NIORPI.of().primaryGpu(gpus);
        } catch (Exception ex) {
            if (gpus == null || gpus.isEmpty()) {
                return NOptional.ofEmpty();
            }
            String forced = System.getProperty(PRIMARY_GPU_PROPERTY);
            if (forced != null && !forced.trim().isEmpty()) {
                String f = forced.trim();
                for (NGpuDevice g : gpus) {
                    if (g != null && f.equals(g.capability(PCI_BUS_ID).orNull())) {
                        return NOptional.of(g);
                    }
                }
            }
            NGpuDevice best = null;
            for (NGpuDevice g : gpus) {
                if (g == null || !g.isComputeCapable()) {
                    continue;
                }
                if (best == null) {
                    best = g;
                } else {
                    boolean candidateDedicated = g.deviceType() == NGpuDeviceType.DEDICATED_GPU;
                    boolean currentDedicated = best.deviceType() == NGpuDeviceType.DEDICATED_GPU;
                    if (candidateDedicated != currentDedicated) {
                        if (candidateDedicated) {
                            best = g;
                        }
                    } else {
                        long candidateMem = g.vram() == null ? -1 : g.vram().total();
                        long currentMem = best.vram() == null ? -1 : best.vram().total();
                        if (candidateMem > currentMem) {
                            best = g;
                        }
                    }
                }
            }
            return best == null ? NOptional.<NGpuDevice>ofEmpty() : NOptional.of(best);
        }
    }
}
