/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.platform;

import net.thevpc.nuts.util.NBlankable;

import java.util.Objects;

/**
 * A single GPU device present on the machine.
 * <p>
 * Instances describe <em>hardware</em>, they say nothing about which compute
 * runtime is installed to drive it, that being the responsibility of
 * {@link NParallelProcessorFamily}. The two axes are intentionally separate
 * because the relation is not one to one : a runtime such as Vulkan or OpenCL
 * runs on any vendor's hardware, while a single physical device is typically
 * reachable through several runtimes at once.
 * <p>
 * {@link #getPciBusId()} is the stable identity of a device and is the key to
 * use when merging results coming from different sources, since the same
 * physical GPU is reported once per runtime that can reach it.
 * <p>
 * Optional values are reported with an explicit sentinel rather than null :
 * {@link #getComputeCapability()} and {@link #getTotalMemoryBytes()} return a
 * negative value when unknown, string properties return null when unknown.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.9
 */
public class NGpuDevice implements NBlankable {

    /**
     * Minimum compute capability for fast half precision arithmetic, NVIDIA Pascal.
     */
    public static final int CC_PASCAL = 600;

    /**
     * Minimum compute capability for {@code __dp4a}, the byte-wise dot product
     * intrinsic backing integer 8 bits arithmetic.
     */
    public static final int CC_DP4A = 610;

    /**
     * Minimum compute capability for tensor core matrix operations, NVIDIA Turing.
     */
    public static final int CC_TURING = 750;

    /**
     * Minimum compute capability for bfloat16, NVIDIA Ampere.
     */
    public static final int CC_AMPERE = 800;

    /**
     * Turing parts that reach {@link #CC_TURING} yet ship without tensor cores,
     * so that the compute capability alone would overstate their capabilities.
     * <p>
     * impl-note: list borrowed from llama.cpp, which special cases the very same
     * models by marketing name because no property exposed by the driver
     * distinguishes them.
     */
    private static final String[] TURING_WITHOUT_TENSOR_CORES_EXACT = {
            "NVIDIA GeForce MX450",
            "NVIDIA GeForce MX550"
    };

    /**
     * Model name prefix of Turing parts shipped without tensor cores, covering
     * the whole GeForce GTX 16 series.
     */
    private static final String TURING_WITHOUT_TENSOR_CORES_PREFIX = "NVIDIA GeForce GTX 16";

    private final NGpuVendor vendor;
    private final NGpuDeviceType deviceType;
    private final String pciBusId;
    private final String pciDeviceId;
    private final String kernelDriver;
    private final String modelName;
    private final String uuid;
    private final int computeCapability;
    private final long totalMemoryBytes;
    private final String driverVersion;

    private NGpuDevice(NGpuVendor vendor, NGpuDeviceType deviceType, String pciBusId, String pciDeviceId,
                       String kernelDriver, String modelName, String uuid, int computeCapability,
                       long totalMemoryBytes, String driverVersion) {
        this.vendor = vendor == null ? NGpuVendor.UNKNOWN : vendor;
        this.deviceType = deviceType == null ? NGpuDeviceType.UNKNOWN : deviceType;
        this.pciBusId = pciBusId;
        this.pciDeviceId = pciDeviceId;
        this.kernelDriver = kernelDriver;
        this.modelName = modelName;
        this.uuid = uuid;
        this.computeCapability = computeCapability;
        this.totalMemoryBytes = totalMemoryBytes;
        this.driverVersion = driverVersion;
    }

    /**
     * Creates a device description.
     *
     * @param vendor            hardware vendor
     * @param deviceType        dedicated or integrated
     * @param pciBusId          pci address in the {@code 0000:01:00.0} form, stable identity of the device
     * @param pciDeviceId       raw pci device id in the {@code 0x28e1} form
     * @param kernelDriver      name of the kernel module bound to the device, null when none is bound
     * @param modelName         marketing name, null when unknown
     * @param uuid              vendor assigned device uuid, null when unknown
     * @param computeCapability compute capability encoded as {@code major * 100 + minor * 10}, negative when unknown
     * @param totalMemoryBytes  total device memory in bytes, negative when unknown
     * @param driverVersion     vendor driver version, null when unknown
     * @return a new device description
     */
    public static NGpuDevice of(NGpuVendor vendor, NGpuDeviceType deviceType, String pciBusId, String pciDeviceId,
                                String kernelDriver, String modelName, String uuid, int computeCapability,
                                long totalMemoryBytes, String driverVersion) {
        return new NGpuDevice(vendor, deviceType, pciBusId, pciDeviceId, kernelDriver, modelName, uuid,
                computeCapability, totalMemoryBytes, driverVersion);
    }

    /**
     * Hardware vendor.
     *
     * @return hardware vendor, never null
     */
    public NGpuVendor getVendor() {
        return vendor;
    }

    /**
     * Device kind, which decides how memory amounts are to be interpreted.
     *
     * @return device kind, never null
     */
    public NGpuDeviceType getDeviceType() {
        return deviceType;
    }

    /**
     * Pci address, as an example {@code 0000:01:00.0}. This is the stable
     * identity of the device across sources and runtimes.
     *
     * @return pci address, null when unknown
     */
    public String getPciBusId() {
        return pciBusId;
    }

    /**
     * Raw pci device id, as an example {@code 0x28e1}.
     *
     * @return raw pci device id, null when unknown
     */
    public String getPciDeviceId() {
        return pciDeviceId;
    }

    /**
     * Kernel module currently bound to the device, as an example {@code nvidia},
     * {@code nouveau} or {@code i915}.
     *
     * @return bound kernel module, null when none is bound
     */
    public String getKernelDriver() {
        return kernelDriver;
    }

    /**
     * Marketing name, as an example {@code NVIDIA GeForce RTX 4050 Laptop GPU}.
     *
     * @return marketing name, null when unknown
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Vendor assigned device uuid.
     *
     * @return device uuid, null when unknown
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Compute capability encoded as {@code major * 100 + minor * 10}, so that
     * compute capability 8.9 is reported as {@code 890} and capabilities remain
     * directly comparable.
     *
     * @return encoded compute capability, negative when unknown
     */
    public int getComputeCapability() {
        return computeCapability;
    }

    /**
     * Total device memory. Only an absolute figure when
     * {@link #getDeviceType()} is dedicated, see {@link NGpuDeviceType}.
     *
     * @return total memory in bytes, negative when unknown
     */
    public long getTotalMemoryBytes() {
        return totalMemoryBytes;
    }

    /**
     * Vendor driver version, as an example {@code 580.159.03}.
     *
     * @return driver version, null when unknown
     */
    public String getDriverVersion() {
        return driverVersion;
    }

    /**
     * Returns true when the vendor's own compute stack can actually drive this
     * device, which requires the vendor's kernel module to be the one bound.
     * <p>
     * An NVIDIA device bound to {@code nouveau} returns false : the hardware is
     * present and drives the display, yet no CUDA capability is available.
     *
     * @return true when the vendor compute stack is usable
     */
    public boolean isComputeCapable() {
        String expected = vendor.getComputeKernelModule();
        return expected != null && expected.equals(kernelDriver);
    }

    /**
     * Returns true when the compute capability is known.
     *
     * @return true when {@link #getComputeCapability()} is meaningful
     */
    public boolean hasComputeCapability() {
        return computeCapability >= 0;
    }

    /**
     * Returns true when tensor cores are available.
     * <p>
     * Compute capability alone is not sufficient here, a handful of Turing parts
     * reach {@link #CC_TURING} without shipping tensor cores and are excluded by
     * model name.
     *
     * @return true when tensor cores are available
     */
    public boolean hasTensorCores() {
        if (vendor != NGpuVendor.NVIDIA || computeCapability < CC_TURING) {
            return false;
        }
        if (modelName != null) {
            for (String s : TURING_WITHOUT_TENSOR_CORES_EXACT) {
                if (s.equals(modelName)) {
                    return false;
                }
            }
            if (modelName.startsWith(TURING_WITHOUT_TENSOR_CORES_PREFIX)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true when half precision arithmetic is supported.
     *
     * @return true when fp16 is supported
     */
    public boolean isSupportedFp16() {
        return vendor == NGpuVendor.NVIDIA && computeCapability >= CC_PASCAL;
    }

    /**
     * Returns true when bfloat16 arithmetic is supported.
     *
     * @return true when bf16 is supported
     */
    public boolean isSupportedBf16() {
        return vendor == NGpuVendor.NVIDIA && computeCapability >= CC_AMPERE;
    }

    /**
     * Returns true when integer 8 bits arithmetic is supported, which the
     * {@code __dp4a} intrinsic provides from Pascal onwards.
     *
     * @return true when int8 is supported
     */
    public boolean isSupportedInt8() {
        return vendor == NGpuVendor.NVIDIA && computeCapability >= CC_DP4A;
    }

    /**
     * Returns true when integer 4 bits arithmetic is supported, which is a
     * tensor core only capability.
     *
     * @return true when int4 is supported
     */
    public boolean isSupportedInt4() {
        return hasTensorCores();
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(pciBusId) && vendor == NGpuVendor.UNKNOWN;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendor, deviceType, pciBusId, pciDeviceId, kernelDriver, modelName, uuid,
                computeCapability, totalMemoryBytes, driverVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NGpuDevice that = (NGpuDevice) o;
        return computeCapability == that.computeCapability
                && totalMemoryBytes == that.totalMemoryBytes
                && vendor == that.vendor
                && deviceType == that.deviceType
                && Objects.equals(pciBusId, that.pciBusId)
                && Objects.equals(pciDeviceId, that.pciDeviceId)
                && Objects.equals(kernelDriver, that.kernelDriver)
                && Objects.equals(modelName, that.modelName)
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(driverVersion, that.driverVersion);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(modelName == null ? vendor.id() : modelName);
        sb.append(" [").append(pciBusId).append(']');
        sb.append(" type=").append(deviceType.id());
        if (kernelDriver != null) {
            sb.append(" driver=").append(kernelDriver);
        }
        if (hasComputeCapability()) {
            sb.append(" cc=").append(computeCapability / 100).append('.').append((computeCapability / 10) % 10);
        }
        if (totalMemoryBytes >= 0) {
            sb.append(" memory=").append(totalMemoryBytes / (1024 * 1024)).append("MiB");
        }
        return sb.toString();
    }
}
