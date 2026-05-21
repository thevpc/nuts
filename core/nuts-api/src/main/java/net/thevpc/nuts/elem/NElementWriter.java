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
import net.thevpc.nuts.time.NProgressFactory;

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


    static NElementWriter of() {
        return NExtensions.of(NElementWriter.class);
    }

    static NElementWriter ofPlainJson() {
        return of().ntf(false).json();
    }

    static NElementWriter ofJson() {
        return of().ntf(false).json();
    }

    static NElementWriter ofPlainProps() {
        return of().ntf(false).contentType(NContentType.PROPS);
    }

    static NElementWriter ofProps() {
        return of().ntf(false).contentType(NContentType.PROPS);
    }

    static NElementWriter ofPlainXml() {
        return of().ntf(false).contentType(NContentType.XML);
    }

    static NElementWriter ofXml() {
        return of().ntf(false).contentType(NContentType.XML);
    }

    static NElementWriter ofPlainTree() {
        return of().ntf(false).contentType(NContentType.TREE);
    }

    static NElementWriter ofTree() {
        return of().ntf(false).contentType(NContentType.TREE);
    }

    static NElementWriter ofPlain() {
        return of().ntf(false).contentType(NContentType.PLAIN);
    }

    static NElementWriter ofPlainTson() {
        return of().ntf(false).contentType(NContentType.TSON);
    }

    static NElementWriter ofTson() {
        return of().ntf(false).contentType(NContentType.TSON);
    }

    static NElementWriter ofPlainYaml() {
        return of().ntf(false).contentType(NContentType.YAML);
    }

    static NElementWriter ofYaml() {
        return of().ntf(false).contentType(NContentType.YAML);
    }

    static NElementWriter ofPlainTable() {
        return of().ntf(false).contentType(NContentType.TABLE);
    }

    static NElementWriter ofTable() {
        return of().ntf(false).contentType(NContentType.TABLE);
    }

    static NElementWriter ofNtfJson() {
        return of().ntf(true).json();
    }

    static NElementWriter ofNtfProps() {
        return of().ntf(true).contentType(NContentType.PROPS);
    }

    static NElementWriter ofNtfXml() {
        return of().ntf(true).contentType(NContentType.XML);
    }

    static NElementWriter ofNtfTree() {
        return of().ntf(true).contentType(NContentType.TREE);
    }

    static NElementWriter ofNtfTson() {
        return of().ntf(true).contentType(NContentType.TSON);
    }

    static NElementWriter ofNtfYaml() {
        return of().ntf(true).contentType(NContentType.YAML);
    }

    static NElementWriter ofNtfTable() {
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

    NElementWriter json();

    NElementWriter yaml();

    NElementWriter tson();

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

    NElementFormatter formatter();

    NElementWriter formatter(NElementFormatter formatter);

    NElementWriter compact(boolean compact);

    NIterableFormat iter(NPrintStream out);

    boolean isLogProgress();

    NElementWriter setLogProgress(boolean logProgress);

    boolean isTraceProgress();

    NElementWriter setTraceProgress(boolean traceProgress);

    NProgressFactory progressFactory();

    NElementWriter progressFactory(NProgressFactory progressFactory);

    NElementMapperStore mapperStore();

    NElementWriter doWithMapperStore(Consumer<NElementMapperStore> doWith);

    NObjectWriter formatterCompact();

    NObjectWriter formatterPretty();

    NObjectWriter formatterVerbatim();

    NObjectWriter formatter(NElementFormatterStyle style);
}
