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
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.reflect.NClassLoader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

/**
 * Diagnostic utility explaining class and resource resolution paths across
 * leaves and composites.
 *
 * @app.category Diagnostics
 */
public final class NClassLoaderDiagnostics {

    private NClassLoaderDiagnostics() {
    }

    /**
     * Walks the resolution tiers and explains how the given class is or is not resolved.
     *
     * @param from      initiating classloader
     * @param className fully qualified class name
     * @return diagnostic report
     */
    public static String explainClass(ClassLoader from, String className) {
        StringBuilder sb = new StringBuilder();
        sb.append("Resolution explanation for class '").append(className).append("'\n");
        sb.append("From ClassLoader: ").append(from).append("\n");

        if (from == null) {
            sb.append("  [ERROR] ClassLoader is null\n");
            return sb.toString();
        }

        if (from instanceof DefaultNLeafClassLoader) {
            DefaultNLeafClassLoader leaf = (DefaultNLeafClassLoader) from;
            for (NClassLoaderResolution.NResolutionTier<Class<?>> tier : DefaultNLeafClassLoader.CLASS_TIERS) {
                sb.append("  Tier '").append(tier.name()).append("': ");
                try {
                    Class<?> c = tier.tryResolve(leaf, className);
                    if (c != null) {
                        sb.append("HIT -> ").append(c.getName())
                                .append(" (defined by ").append(c.getClassLoader()).append(")\n");
                        break;
                    } else {
                        sb.append("MISS\n");
                    }
                } catch (Exception ex) {
                    sb.append("ERROR (").append(ex.getMessage()).append(")\n");
                }
            }
        } else if (from instanceof NClassLoaderBase) {
            NClassLoaderBase base = (NClassLoaderBase) from;
            sb.append("  Composite ClassLoader (NClassLoaderBase):\n");
            try {
                ClassLoader p = base.getParent();
                if (p != null) {
                    sb.append("    Parent (").append(p).append("): ");
                    try {
                        Class<?> c = p.loadClass(className);
                        sb.append("HIT -> ").append(c).append("\n");
                    } catch (ClassNotFoundException e) {
                        sb.append("MISS\n");
                    }
                }
                sb.append("    Children (").append(base.children.size()).append("):\n");
                for (NClassLoader child : base.children) {
                    sb.append("      Child ").append(child).append(": ");
                    try {
                        Class<?> c = child.loadClass(className);
                        sb.append("HIT -> ").append(c).append("\n");
                    } catch (ClassNotFoundException e) {
                        sb.append("MISS\n");
                    }
                }
            } catch (Exception ex) {
                sb.append("    ERROR: ").append(ex.getMessage()).append("\n");
            }
        } else if (from instanceof DefaultNCompositeClassLoader) {
            DefaultNCompositeClassLoader comp = (DefaultNCompositeClassLoader) from;
            sb.append("  Composite ClassLoader (DefaultNCompositeClassLoader):\n");
            ClassLoader p = comp.getParent();
            if (p != null) {
                sb.append("    Parent (").append(p).append("): ");
                try {
                    Class<?> c = p.loadClass(className);
                    sb.append("HIT -> ").append(c).append("\n");
                } catch (ClassNotFoundException e) {
                    sb.append("MISS\n");
                }
            }
            sb.append("    Children (").append(comp.children().size()).append("):\n");
            for (NClassLoader child : comp.children()) {
                sb.append("      Child ").append(child).append(": ");
                try {
                    Class<?> c = child.loadClass(className);
                    sb.append("HIT -> ").append(c).append("\n");
                } catch (ClassNotFoundException e) {
                    sb.append("MISS\n");
                }
            }
        } else {
            sb.append("  Standard ClassLoader delegation: ");
            try {
                Class<?> c = from.loadClass(className);
                sb.append("HIT -> ").append(c).append("\n");
            } catch (ClassNotFoundException e) {
                sb.append("MISS\n");
            }
        }
        return sb.toString();
    }

    /**
     * Walks the resolution tiers and explains how the given resource is or is not resolved.
     *
     * @param from         initiating classloader
     * @param resourceName resource path
     * @return diagnostic report
     */
    public static String explainResource(ClassLoader from, String resourceName) {
        StringBuilder sb = new StringBuilder();
        sb.append("Resolution explanation for resource '").append(resourceName).append("'\n");
        sb.append("From ClassLoader: ").append(from).append("\n");

        if (from == null) {
            sb.append("  [ERROR] ClassLoader is null\n");
            return sb.toString();
        }

        if (from instanceof DefaultNLeafClassLoader) {
            DefaultNLeafClassLoader leaf = (DefaultNLeafClassLoader) from;
            for (NClassLoaderResolution.NResolutionTier<List<URL>> tier : DefaultNLeafClassLoader.MULTI_RESOURCE_TIERS) {
                sb.append("  Tier '").append(tier.name()).append("': ");
                try {
                    List<URL> urls = tier.tryResolve(leaf, resourceName);
                    if (urls != null && !urls.isEmpty()) {
                        sb.append("HIT (").append(urls.size()).append(" matches) -> ").append(urls).append("\n");
                    } else {
                        sb.append("MISS\n");
                    }
                } catch (Exception ex) {
                    sb.append("ERROR (").append(ex.getMessage()).append(")\n");
                }
            }
        } else {
            sb.append("  ClassLoader.getResources: ");
            try {
                Enumeration<URL> e = from.getResources(resourceName);
                int count = 0;
                while (e.hasMoreElements()) {
                    URL u = e.nextElement();
                    sb.append("\n    - ").append(u);
                    count++;
                }
                if (count == 0) {
                    sb.append("MISS\n");
                } else {
                    sb.append("\n");
                }
            } catch (IOException e) {
                sb.append("ERROR (").append(e.getMessage()).append(")\n");
            }
        }
        return sb.toString();
    }
}
