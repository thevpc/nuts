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
 * <br> ====================================================================
 */
package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Class responsible for manipulating {@link NElement} type. It help parsing
 * from, converting to and formatting such types.
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NElementRPI extends NComponent {


    static NElementRPI of() {
        return NExtensions.of(NElementRPI.class);
    }

    NElements createSharedElements();


    /**
     * compile pathExpression into a valid NutsElementPath that helps filtering
     * elements tree. JSONPath expressions refer to a JSON structure the same
     * way as XPath expression are used with XML documents. JSONPath expressions
     * can use the dot notation and/or bracket notations .store.book[0].title
     * The trailing root is not necessary : .store.book[0].title You can also
     * use bracket notation store['book'][0].title for input paths.
     *
     * @param pathExpression element path expression
     * @return Element Path filter
     */
    NElementSelector compileSelector(String pathExpression);

    NElementType commonNumberType(NElementType aa, NElementType bb);

    NExprElementReshaper createExprElementReshaper(NExprElementReshaperType type);

    NExprElementReshaperBuilder createExprElementReshaperBuilder(NExprElementReshaperType type);

    NElementFormatterBuilder createElementFormatterBuilder();

    NElementFormatter createElementFormatter(NElementFormatterStyle style);

    NElementPath createRootPath();

    NElementMetadata createElementMetadata();

    NElementMetadata createElementMetadata(Object key, Object value);

    NElementMetadata createElementMetadata(Map<Object, Object> any);

    NElementStep createStepChild(String name);

    NElementStep createStepChild(int index);

    NElementStep createStepParam(String name);

    NElementStep createStepParam(int index);

    NElementStep createStepAnnotationParam(int paramIndex, String name);

    NElementStep createStepAnnotationParam(int paramIndex, int index);

    NElementStep createStepSubList(int index);

    NElementNavigator createRootNavigator(NElement element);
}
