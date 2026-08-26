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
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.text.NContentTypeWriter;
import net.thevpc.nuts.text.NIterableFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NObjectWriter;
import net.thevpc.nuts.mon.NProgressFactory;

import java.util.function.Consumer;

/**
 * Class responsible of manipulating {@link NElement} type. It help parsing
 * from, converting to and formatting such types.
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NElementWriter extends NContentTypeWriter {


    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NElementWriter of() {
        return NExtensions.of(NElementWriter.class);
    }

    /**
     * Creates a new instance of of plain json.
     *
     * @return of plain json result
     */
    static NElementWriter ofPlainJson() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).json( ).ntf(false).json(
         * @return of result
         */
        return of().ntf(false).json();
    }

    /**
     * Creates a new instance of of json.
     *
     * @return of json result
     */
    static NElementWriter ofJson() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).json( ).ntf(false).json(
         * @return of result
         */
        return of().ntf(false).json();
    }

    /**
     * Creates a new instance of of plain props.
     *
     * @return of plain props result
     */
    static NElementWriter ofPlainProps() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.PROPS ).ntf(false).content type(n content type.props
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.PROPS);
    }

    /**
     * Creates a new instance of of props.
     *
     * @return of props result
     */
    static NElementWriter ofProps() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.PROPS ).ntf(false).content type(n content type.props
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.PROPS);
    }

    /**
     * Creates a new instance of of plain xml.
     *
     * @return of plain xml result
     */
    static NElementWriter ofPlainXml() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.XML ).ntf(false).content type(n content type.xml
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.XML);
    }

    /**
     * Creates a new instance of of xml.
     *
     * @return of xml result
     */
    static NElementWriter ofXml() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.XML ).ntf(false).content type(n content type.xml
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.XML);
    }

    /**
     * Creates a new instance of of plain tree.
     *
     * @return of plain tree result
     */
    static NElementWriter ofPlainTree() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.TREE ).ntf(false).content type(n content type.tree
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.TREE);
    }

    /**
     * Creates a new instance of of tree.
     *
     * @return of tree result
     */
    static NElementWriter ofTree() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.TREE ).ntf(false).content type(n content type.tree
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.TREE);
    }

    /**
     * Creates a new instance of of plain.
     *
     * @return of plain result
     */
    static NElementWriter ofPlain() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.PLAIN ).ntf(false).content type(n content type.plain
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.PLAIN);
    }

    /**
     * Creates a new instance of of plain tson.
     *
     * @return of plain tson result
     */
    static NElementWriter ofPlainTson() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.TSON ).ntf(false).content type(n content type.tson
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.TSON);
    }

    /**
     * Creates a new instance of of tson.
     *
     * @return of tson result
     */
    static NElementWriter ofTson() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.TSON ).ntf(false).content type(n content type.tson
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.TSON);
    }

    /**
     * Creates a new instance of of plain yaml.
     *
     * @return of plain yaml result
     */
    static NElementWriter ofPlainYaml() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.YAML ).ntf(false).content type(n content type.yaml
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.YAML);
    }

    /**
     * Creates a new instance of of yaml.
     *
     * @return of yaml result
     */
    static NElementWriter ofYaml() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.YAML ).ntf(false).content type(n content type.yaml
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.YAML);
    }

    /**
     * Creates a new instance of of plain table.
     *
     * @return of plain table result
     */
    static NElementWriter ofPlainTable() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.TABLE ).ntf(false).content type(n content type.table
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.TABLE);
    }

    /**
     * Creates a new instance of of table.
     *
     * @return of table result
     */
    static NElementWriter ofTable() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).contentType(NContentType.TABLE ).ntf(false).content type(n content type.table
         * @return of result
         */
        return of().ntf(false).contentType(NContentType.TABLE);
    }

    /**
     * Creates a new instance of of ntf json.
     *
     * @return of ntf json result
     */
    static NElementWriter ofNtfJson() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(true).json( ).ntf(true).json(
         * @return of result
         */
        return of().ntf(true).json();
    }

    /**
     * Creates a new instance of of ntf props.
     *
     * @return of ntf props result
     */
    static NElementWriter ofNtfProps() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(true).contentType(NContentType.PROPS ).ntf(true).content type(n content type.props
         * @return of result
         */
        return of().ntf(true).contentType(NContentType.PROPS);
    }

    /**
     * Creates a new instance of of ntf xml.
     *
     * @return of ntf xml result
     */
    static NElementWriter ofNtfXml() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(true).contentType(NContentType.XML ).ntf(true).content type(n content type.xml
         * @return of result
         */
        return of().ntf(true).contentType(NContentType.XML);
    }

    /**
     * Creates a new instance of of ntf tree.
     *
     * @return of ntf tree result
     */
    static NElementWriter ofNtfTree() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(true).contentType(NContentType.TREE ).ntf(true).content type(n content type.tree
         * @return of result
         */
        return of().ntf(true).contentType(NContentType.TREE);
    }

    /**
     * Creates a new instance of of ntf tson.
     *
     * @return of ntf tson result
     */
    static NElementWriter ofNtfTson() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(true).contentType(NContentType.TSON ).ntf(true).content type(n content type.tson
         * @return of result
         */
        return of().ntf(true).contentType(NContentType.TSON);
    }

    /**
     * Creates a new instance of of ntf yaml.
     *
     * @return of ntf yaml result
     */
    static NElementWriter ofNtfYaml() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(true).contentType(NContentType.YAML ).ntf(true).content type(n content type.yaml
         * @return of result
         */
        return of().ntf(true).contentType(NContentType.YAML);
    }

    /**
     * Creates a new instance of of ntf table.
     *
     * @return of ntf table result
     */
    static NElementWriter ofNtfTable() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(true).contentType(NContentType.TABLE ).ntf(true).content type(n content type.table
         * @return of result
         */
        return of().ntf(true).contentType(NContentType.TABLE);
    }

    /**
     * return parse content type
     *
     * @return content type
     * @since 0.8.1
     */
    NContentType contentType();

    /**
     * set the parse content type. defaults to JSON. Non structured content
     * types are not allowed.
     *
     * @param contentType contentType
     * @return {@code this} instance
     * @since 0.8.1
     */
    NElementWriter contentType(NContentType contentType);

    /**
     * Json.
     *
     * @return json result
     */
    NElementWriter json();

    /**
     * Yaml.
     *
     * @return yaml result
     */
    NElementWriter yaml();

    /**
     * Tson.
     *
     * @return tson result
     */
    NElementWriter tson();

    /**
     * Xml.
     *
     * @return xml result
     */
    NElementWriter xml();

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
    NElementWriter configure(boolean skipUnsupported, String... args);

    @Override
    NElementWriter ntf(boolean ntf);

    /**
     * Formatter.
     *
     * @return formatter result
     */
    NElementFormatter formatter();

    /**
     * Formatter.
     *
     * @param formatter formatter
     * @return formatter result
     */
    NElementWriter formatter(NElementFormatter formatter);

    /**
     * Compact.
     *
     * @param compact compact
     * @return compact result
     */
    NElementWriter compact(boolean compact);

    /**
     * Iter.
     *
     * @param out out
     * @return iter result
     */
    NIterableFormat iter(NPrintStream out);

    /**
     * Checks if is log progress.
     *
     * @return is log progress result
     */
    boolean isLogProgress();

    /**
     * Sets the log progress.
     *
     * @param logProgress log progress
     * @return set log progress result
     */
    NElementWriter setLogProgress(boolean logProgress);

    /**
     * Checks if is trace progress.
     *
     * @return is trace progress result
     */
    boolean isTraceProgress();

    /**
     * Sets the trace progress.
     *
     * @param traceProgress trace progress
     * @return set trace progress result
     */
    NElementWriter setTraceProgress(boolean traceProgress);

    /**
     * Progress factory.
     *
     * @return progress factory result
     */
    NProgressFactory progressFactory();

    /**
     * Progress factory.
     *
     * @param progressFactory progress factory
     * @return progress factory result
     */
    NElementWriter progressFactory(NProgressFactory progressFactory);

    /**
     * Mapper store.
     *
     * @return mapper store result
     */
    NElementMapperStore mapperStore();

    /**
     * Do with mapper store.
     *
     * @param doWith do with
     * @return do with mapper store result
     */
    NElementWriter doWithMapperStore(Consumer<NElementMapperStore> doWith);

    /**
     * Formatter compact.
     *
     * @return formatter compact result
     */
    NObjectWriter formatterCompact();

    /**
     * Formatter pretty.
     *
     * @return formatter pretty result
     */
    NObjectWriter formatterPretty();

    /**
     * Formatter verbatim.
     *
     * @return formatter verbatim result
     */
    NObjectWriter formatterVerbatim();

    /**
     * Formatter.
     *
     * @param style style
     * @return formatter result
     */
    NObjectWriter formatter(NElementFormatterStyle style);
}
