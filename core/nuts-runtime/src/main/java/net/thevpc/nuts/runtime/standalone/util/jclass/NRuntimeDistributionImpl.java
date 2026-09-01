/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.core.NConfigItem;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.elem.NToElement;
import net.thevpc.nuts.platform.NRuntimeDistribution;
import net.thevpc.nuts.platform.NRuntimeDistributionFamily;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.Objects;

/**
 * SDK location
 *
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public class NRuntimeDistributionImpl extends NConfigItem implements NRuntimeDistribution, NToElement {
    public static final long serialVersionUID = 3;
    private final NId id;
    private final NRuntimeDistributionFamily family;
    private final String name;
    private final String variant;
    private final String vendor;
    private final String packaging;
    private final String product;
    private final String path;
    private final String version;
    private final int priority;

    public NRuntimeDistributionImpl(NElement element) {
        NAssert.requireNamedNonNull(element,"element");
        NObjectElement o = element.asObject().get();
        id=o.get("id").flatMap(NElement::asStringValue).flatMap(NId::get).orNull();
        family =o.get("family").flatMap(NElement::asStringValue).flatMap(NRuntimeDistributionFamily::parse).orNull();
        name=o.get("name").flatMap(NElement::asStringValue).orNull();
        variant=o.get("variant").flatMap(NElement::asStringValue).orNull();
        vendor=o.get("vendor").flatMap(NElement::asStringValue).orNull();
        packaging=o.get("packaging").flatMap(NElement::asStringValue).orNull();
        product=o.get("product").flatMap(NElement::asStringValue).orNull();
        path=o.get("path").flatMap(NElement::asStringValue).orNull();
        version=o.get("version").flatMap(NElement::asStringValue).orNull();
        priority=o.get("priority").flatMap(NElement::asIntValue).orElse(0);
    }

    @Override
    public NElement toElement() {
        return NElement.ofObjectBuilder()
                .set("id",id==null?null:id.toString())
                .set("family", family ==null?null: family.id())
                .set("name",name)
                .set("variant",variant)
                .set("vendor",vendor)
                .set("packaging",packaging)
                .set("product",product)
                .set("path",path)
                .set("version",version)
                .build();
    }

    /**
     * default constructor
     *
     * @param id        id
     * @param vendor    SDK oracle/openjdk
     * @param product   SDK product. In java this is Oracle JDK or OpenJDK.
     * @param variant   hotspot/graalvm
     * @param packaging SDK packaging. for Java SDK this is room to set JRE or JDK.
     * @param path      SDK path
     * @param name      SDK name
     * @param version   SDK version
     * @param priority  SDK priority
     */
    public NRuntimeDistributionImpl(NId id, String vendor, String product, String variant, String name, String path, String version, String packaging, int priority) {
        this.id = id;
        this.family = (id == null || NBlankable.isBlank(id.artifactId())) ? NRuntimeDistributionFamily.JAVA :
                NRuntimeDistributionFamily.parse(id.artifactId()).orElse(NRuntimeDistributionFamily.UNKNOWN);
        this.product = product;
        this.variant = variant;
        this.vendor = vendor;
        this.name = name;
        this.path = path;
        this.version = version;
        this.packaging = packaging;
        this.priority = priority;
    }

    /**
     * Variant.
     *
     * @return variant result
     */
    @NGetter
    @Override
    public String variant() {
        return variant;
    }

    /**
     * Vendor.
     *
     * @return vendor result
     */
    @NGetter
    @Override
    public String vendor() {
        return vendor;
    }

    /**
     * Priority.
     *
     * @return priority result
     */
    @NGetter
    @Override
    public int priority() {
        return priority;
    }

    /**
     * Priority.
     *
     * @param priority priority
     * @return priority result
     */
    @NSetter
    @Override
    public NRuntimeDistribution priority(int priority) {
        return new NRuntimeDistributionImpl(id, vendor,product, variant,name, path, version, packaging, priority);
    }

    /**
     * Id.
     *
     * @return id result
     */
    @NGetter
    @Override
    public NId id() {
        return id;
    }

    /**
     * Execution engine family.
     *
     * @return execution engine family result
     */
    @NGetter
    @Override
    public NRuntimeDistributionFamily family() {
        return family;
    }

    /**
     * SDK product. In java this is
     * Oracle JDK or OpenJDK.
     *
     * @return product name
     */
    @NGetter
    @Override
    public String product() {
        return product;
    }

    /**
     * SDK version
     *
     * @return SDK version
     */
    @NGetter
    @Override
    public String version() {
        return version;
    }

    /**
     * sdk name
     *
     * @return sdk name
     */
    @NGetter
    @Override
    public String name() {
        return name;
    }

    /**
     * sdk path
     *
     * @return sdk path
     */
    @NGetter
    @Override
    public String path() {
        return path;
    }

    /**
     * sdk packaging. for Java SDK this
     * is room to set JRE or JDK.
     *
     * @return packaging name
     */
    @NGetter
    @Override
    public String packaging() {
        return packaging;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, packaging, product, path, version, priority);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NRuntimeDistributionImpl that = (NRuntimeDistributionImpl) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(packaging, that.packaging) &&
                Objects.equals(product, that.product) &&
                Objects.equals(path, that.path) &&
                Objects.equals(version, that.version) &&
                Objects.equals(priority, that.priority)
                ;
    }

    @Override
    public String toString() {
        return "NRuntimeDistribution{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", packaging='" + packaging + '\'' +
                ", product='" + product + '\'' +
                ", path='" + path + '\'' +
                ", version='" + version + '\'' +
                ", priority=" + priority +
                '}';
    }

    /**
     * Copy.
     *
     * @return copy result
     */
    @Override
    public NRuntimeDistribution copy() {
        try {
            NRuntimeDistribution cloned = (NRuntimeDistribution) clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            /**
             * Runtime exception.
             *
             * @param e e
             * @return runtime exception result
             */
            throw new NUnexpectedException(NMsg.ofC("clone unsupported for %s",getClass()),e);
        }
    }
}
