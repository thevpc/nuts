/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
 * <p>
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
package net.thevpc.nuts.time;

/**
 * NutsProgressFactory is responsible of creating instances of {@link NProgressListener}
 *
 * @author thevpc
 * @app.category Toolkit
 * @since 0.5.8
 */
public interface NProgressFactory {

    /**
     * create a new instance of {@link NProgressListener}
     *
     * @param source       source object of the progress. This may be the File for instance
     * @param sourceOrigin source origin object of the progress. This may be the NutsId for instance
     * @return new instance of {@link NProgressListener}
     */
    NProgressListener createProgressListener(Object source, Object sourceOrigin);

}
