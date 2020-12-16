/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.NutsIdLocation;

import java.util.Objects;

public class DefaultNutsIdLocation implements NutsIdLocation {
    private final String url;
    private final String classifier;
    private final String region;

    public DefaultNutsIdLocation(String url, String classifier, String region) {
        this.url = url;
        this.classifier = classifier;
        this.region = region;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public String getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsIdLocation that = (DefaultNutsIdLocation) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(classifier, that.classifier) &&
                Objects.equals(region, that.region)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, classifier, region);
    }
}
