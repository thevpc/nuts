/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
//package net.thevpc.nuts;
//
///**
// * Exception thrown when extension could not be resolved.
// *
// * @app.category Exceptions
// * @since 0.5.4
// */
//public class NExtensionNotFoundException extends NExtensionException {
//
//    /**
//     * missing type
//     */
//    private final Class missingType;
//
//    /**
//     * extension name
//     */
//    private final Object criteria;
//
//    /**
//     * Constructs a new NutsExtensionNotFoundException exception
//     *
//     * @param session     workspace
//     * @param missingType missing type
//     * @param criteria    extension support criteria object
//     */
//    public NExtensionNotFoundException(Class missingType, Object criteria) {
//        super(null,
//                criteria == null ?
//                        NMsg.ofC(
//                                "extension %s could not found: type %s could not be wired",
//                                missingType.getSimpleName(),
//                                missingType.getName()
//                        ) :
//                        NMsg.ofC(
//                                "extension %s could not found: type %s could not be wired with %s",
//                                missingType.getSimpleName(),
//                                missingType.getName(),
//                                criteria
//                        )
//                , null);
//        this.missingType = missingType;
//        this.criteria = criteria;
//    }
//
//    /**
//     * missing type
//     *
//     * @return missing type
//     */
//    public Class getMissingType() {
//        return missingType;
//    }
//
//
//    /**
//     * extension name
//     *
//     * @return extension name
//     */
//    public Object getCriteria() {
//        return criteria;
//    }
//}
