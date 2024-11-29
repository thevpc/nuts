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
 *
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
package net.thevpc.nuts.env;

import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.util.NBlankable;

import java.util.Objects;

/**
 * This class is used in {@link NDescriptor} to describe
 * locations/mirrors to download artifact content instead of the
 * regular location.
 *
 * @app.category Descriptor
 */
public class NIdLocation implements NBlankable {
    private final String url;
    private final String region;
    private final String classifier;

    public NIdLocation(String url, String region, String classifier) {
        this.url = url;
        this.region = region;
        this.classifier = classifier;
    }

    /**
     * location url of the artifact content
     *
     * @return location url of the artifact content
     */
    public String getUrl() {
        return url;
    }

    /**
     * location (geographic) region that may be used to select
     * the most effective mirror
     *
     * @return location (geographic) region that may be used to select the most effective mirror
     */
    public String getRegion() {
        return region;
    }

    /**
     * classifier for the artifact
     *
     * @return classifier for the artifact
     */
    public String getClassifier() {
        return classifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, region, classifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NIdLocation that = (NIdLocation) o;
        return Objects.equals(url, that.url) && Objects.equals(region, that.region) && Objects.equals(classifier, that.classifier);
    }

    @Override
    public String toString() {
        return "NutsIdLocation{" +
                "url='" + url + '\'' +
                ", region='" + region + '\'' +
                ", classifier='" + classifier + '\'' +
                '}';
    }

    @Override
    public boolean isBlank() {
        if (!NBlankable.isBlank(url)) {
            return false;
        }
        if (!NBlankable.isBlank(classifier)) {
            return false;
        }
        return NBlankable.isBlank(region);
    }
}
