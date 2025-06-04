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
package net.thevpc.nuts.elem;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.format.NContentTypeFormat;
import net.thevpc.nuts.format.NIterableFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.time.NProgressFactory;

import java.util.function.Predicate;

/**
 * Class responsible of manipulating {@link NElement} type. It help parsing
 * from, converting to and formatting such types.
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NElementFormat extends NContentTypeFormat {


    static NElementFormat of(Object any) {
        return of().setValue(any);
    }

    static NElementFormat of() {
        return NExtensions.of(NElementFormat.class);
    }

    static NElementFormat ofPlainJson(Object any) {
        return of().setValue(any).setNtf(false).json();
    }

    static NElementFormat ofPlainProps(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.PROPS);
    }

    static NElementFormat ofPlainXml(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.XML);
    }

    static NElementFormat ofPlainTree(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.TREE);
    }

    static NElementFormat ofPlain(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.PLAIN);
    }

    static NElementFormat ofPlainTson(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.TSON);
    }

    static NElementFormat ofPlainYaml(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.YAML);
    }

    static NElementFormat ofPlainTable(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.TABLE);
    }

    static NElementFormat ofNtfJson(Object any) {
        return of().setValue(any).setNtf(true).json();
    }

    static NElementFormat ofNtfProps(Object any) {
        return of().setValue(any).setNtf(true).setContentType(NContentType.PROPS);
    }

    static NElementFormat ofNtfXml(Object any) {
        return of().setValue(any).setNtf(true).setContentType(NContentType.XML);
    }

    static NElementFormat ofNtfTree(Object any) {
        return of().setValue(any).setNtf(true).setContentType(NContentType.TREE);
    }

    static NElementFormat ofNtfTson(Object any) {
        return of().setValue(any).setNtf(true).setContentType(NContentType.TSON);
    }

    static NElementFormat ofNtfYaml(Object any) {
        return of().setValue(any).setNtf(true).setContentType(NContentType.YAML);
    }

    static NElementFormat ofNtfTable(Object any) {
        return of().setValue(any).setNtf(true).setContentType(NContentType.TABLE);
    }

    static NElementFormat ofPlainJson() {
        return of().setNtf(false).json();
    }

    static NElementFormat ofPlainProps() {
        return of().setNtf(false).setContentType(NContentType.PROPS);
    }

    static NElementFormat ofPlainXml() {
        return of().setNtf(false).setContentType(NContentType.XML);
    }

    static NElementFormat ofPlainTree() {
        return of().setNtf(false).setContentType(NContentType.TREE);
    }

    static NElementFormat ofPlain() {
        return of().setNtf(false).setContentType(NContentType.PLAIN);
    }

    static NElementFormat ofPlainTson() {
        return of().setNtf(false).setContentType(NContentType.TSON);
    }

    static NElementFormat ofPlainYaml() {
        return of().setNtf(false).setContentType(NContentType.YAML);
    }

    static NElementFormat ofPlainTable() {
        return of().setNtf(false).setContentType(NContentType.TABLE);
    }

    static NElementFormat ofNtfJson() {
        return of().setNtf(true).json();
    }

    static NElementFormat ofNtfProps() {
        return of().setNtf(true).setContentType(NContentType.PROPS);
    }

    static NElementFormat ofNtfXml() {
        return of().setNtf(true).setContentType(NContentType.XML);
    }

    static NElementFormat ofNtfTree() {
        return of().setNtf(true).setContentType(NContentType.TREE);
    }

    static NElementFormat ofNtfTson() {
        return of().setNtf(true).setContentType(NContentType.TSON);
    }

    static NElementFormat ofNtfYaml() {
        return of().setNtf(true).setContentType(NContentType.YAML);
    }

    static NElementFormat ofNtfTable() {
        return of().setNtf(true).setContentType(NContentType.TABLE);
    }

    /**
     * return parse content type
     *
     * @return content type
     * @since 0.8.1
     */
    NContentType getContentType();

    /**
     * set the parse content type. defaults to JSON. Non structured content
     * types are not allowed.
     *
     * @param contentType contentType
     * @return {@code this} instance
     * @since 0.8.1
     */
    NElementFormat setContentType(NContentType contentType);

    NElementFormat json();

    NElementFormat yaml();

    NElementFormat tson();

    NElementFormat xml();

    /**
     * return current value to format.
     *
     * @return current value to format
     * @since 0.5.6
     */
    @Override
    Object getValue();

    /**
     * set current value to format.
     *
     * @param value value to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    @Override
    NElementFormat setValue(Object value);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NElementFormat configure(boolean skipUnsupported, String... args);

    @Override
    NElementFormat setNtf(boolean ntf);

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
    NElementPath compilePath(String pathExpression);

    /**
     * true is compact json flag is armed
     *
     * @return true is compact json flag is armed
     */
    boolean isCompact();

    /**
     * enable compact json
     *
     * @param compact true to enable compact mode
     * @return {@code this} instance
     */
    NElementFormat setCompact(boolean compact);

    Predicate<Class<?>> getIndestructibleObjects();

    NElementFormat setIndestructibleFormat();

    NElementFormat setIndestructibleObjects(Predicate<Class<?>> destructTypeFilter);

    NIterableFormat iter(NPrintStream out);


    boolean isLogProgress();

    NElementFormat setLogProgress(boolean logProgress);

    boolean isTraceProgress();

    NElementFormat setTraceProgress(boolean traceProgress);

    NProgressFactory getProgressFactory();

    NElementFormat setProgressFactory(NProgressFactory progressFactory);

    NElementMapperStore mappers();
}
