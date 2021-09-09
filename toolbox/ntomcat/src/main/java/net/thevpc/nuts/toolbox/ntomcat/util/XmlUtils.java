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
package net.thevpc.nuts.toolbox.ntomcat.util;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author thevpc
 */
public class XmlUtils {

    public static Stream<Node> stream(final NodeList li) {
        return StreamSupport.stream(iter(li).spliterator(), false);
    }

    public static Stream<Element> streamElements(final NodeList li) {
        return StreamSupport.stream(iter(li).spliterator(), false).filter(Element.class::isInstance)
                .map(Element.class::cast);
    }

    public static Iterable<Node> iter(final NodeList li) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {
                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < li.getLength();
                    }

                    @Override
                    public Node next() {
                        index++;
                        return li.item(index - 1);
                    }
                };
            }
        };
    }
}
