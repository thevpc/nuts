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

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * GPU hardware vendor.
 * <p>
 * This is the <em>hardware</em> axis, deliberately kept distinct from
 * {@link NParallelProcessorFamily} which describes the <em>software</em>
 * runtime available to drive that hardware. A machine commonly exposes several
 * vendors at once, an Intel integrated GPU next to an NVIDIA discrete GPU being
 * the usual laptop layout, and a single runtime such as Vulkan or OpenCL may
 * run on any of them.
 *
 * @author thevpc
 * @app.category Base
 * @since 1.0.0
 */
public enum NGpuVendor implements NEnum {

    /**
     * NVIDIA, PCI vendor id {@code 0x10DE}.
     */
    NVIDIA,

    /**
     * AMD, including legacy ATI, PCI vendor ids {@code 0x1002} and {@code 0x1022}.
     */
    AMD,

    /**
     * Intel, PCI vendor id {@code 0x8086}.
     */
    INTEL,

    /**
     * Apple, PCI vendor id {@code 0x106B}. In practice resolved from the
     * operating system on Apple Silicon rather than from a PCI scan.
     */
    APPLE,

    /**
     * A recognized display adapter from a vendor nuts does not track, which
     * notably covers the paravirtualized adapters exposed by hypervisors.
     */
    OTHER,

    /**
     * The vendor could not be determined.
     */
    UNKNOWN;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NGpuVendor() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Resolves a vendor from the raw PCI vendor id exposed by the linux kernel
     * in {@code /sys/bus/pci/devices/<address>/vendor}. The value is expected in
     * the {@code 0xNNNN} form written by the kernel, a bare hexadecimal value is
     * accepted as well.
     *
     * @param pciVendorId raw pci vendor id, as an example {@code 0x10de}
     * @return resolved vendor, never null
     */
    public static NGpuVendor ofPciVendorId(String pciVendorId) {
        if (pciVendorId == null) {
            return UNKNOWN;
        }
        String v = pciVendorId.trim().toLowerCase();
        if (v.startsWith("0x")) {
            v = v.substring(2);
        }
        if (v.isEmpty()) {
            return UNKNOWN;
        }
        switch (v) {
            case "10de":
                return NVIDIA;
            case "1002":
            case "1022":
                return AMD;
            case "8086":
                return INTEL;
            case "106b":
                return APPLE;
            // paravirtualized adapters : microsoft (wsl, hyper-v), vmware, virtio
            case "1414":
            case "15ad":
            case "1af4":
                return OTHER;
        }
        return OTHER;
    }

    public static NOptional<NGpuVendor> parse(String value) {
        return NEnumUtils.parseEnum(value, NGpuVendor.class, s -> {
            switch (s.normalizedValue()) {
                case "NVIDIA":
                case "GEFORCE":
                case "QUADRO":
                case "TESLA":
                    return NOptional.of(NVIDIA);

                case "AMD":
                case "ATI":
                case "RADEON":
                    return NOptional.of(AMD);

                case "INTEL":
                case "ARC":
                    return NOptional.of(INTEL);

                case "APPLE":
                    return NOptional.of(APPLE);

                case "OTHER":
                    return NOptional.of(OTHER);

                case "UNKNOWN":
                    return NOptional.of(UNKNOWN);
            }
            return null;
        });
    }

    /**
     * Name of the linux kernel module that must be bound to a device for this
     * vendor's own compute stack to have any chance of driving it.
     * <p>
     * The module actually bound is the reliable capability gate : an NVIDIA card
     * driven by {@code nouveau} is perfectly functional for display yet has no
     * CUDA capability at all, which happens on fresh installs and whenever
     * secure boot rejects the proprietary module.
     * <p>
     * The condition is sufficient for {@link #NVIDIA} only, where the module
     * ships the driver api that CUDA needs. For {@link #AMD} it is necessary but
     * not sufficient, ROCm additionally requires the {@code /dev/kfd} node and
     * its userspace. {@link #INTEL} reports none on purpose : {@code i915} is a
     * display driver, Intel compute goes through the separate Level Zero
     * runtime, whose detection is not implemented yet.
     *
     * @return expected kernel module name, or null when the vendor's compute
     * stack cannot be concluded from a bound module
     */
    public String getComputeKernelModule() {
        switch (this) {
            case NVIDIA:
                return "nvidia";
            case AMD:
                return "amdgpu";
        }
        return null;
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    @Override
    public String id() {
        return id;
    }
}
