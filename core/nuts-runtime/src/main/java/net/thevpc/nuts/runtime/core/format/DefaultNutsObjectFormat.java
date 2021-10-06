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
package net.thevpc.nuts.runtime.core.format;

import java.io.File;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.runtime.core.format.plain.NutsObjectFormatPlain;

/**
 *
 * @author thevpc
 */
public class DefaultNutsObjectFormat extends NutsObjectFormatBase {

    private NutsContentType outputFormat;
//    private NutsObjectFormat base;

    public DefaultNutsObjectFormat(NutsWorkspace ws) {
        super(ws, "object-format");
    }

//    public NutsContentType getOutputFormat() {
//        checkSession();
//        NutsContentType t = getSession().getOutputFormat();
//        t=t == null ? NutsContentType.PLAIN : t;
//        if(getSession().isBot()){
//            switch(t){
//                case PLAIN:{
//                    return NutsContentType.PROPS;
//                }
//                case TREE:
//                case TABLE:
//                {
//                    return NutsContentType.JSON;
//                }
//            }
//        }
//        return t;
//    }

    public NutsObjectFormat getBase() {
        checkSession();
        NutsSession session = getSession();
        NutsObjectFormat base = createObjectFormat();
        base.setValue(getValue());
        base.setSession(session);
        base.configure(true, session.boot().getBootOptions().getOutputFormatOptions());
        base.configure(true, session.getOutputFormatOptions());
        return base;
    }

    public NutsObjectFormat createObjectFormat() {
        checkSession();
        NutsSession ws = getSession();
        NutsContentType t = getSession().getOutputFormat();
        if(t==null){
            t = NutsContentType.PLAIN;
            Object v = getValue();
            Object vv = getSession().elem().destruct(v);
            if(vv instanceof Map || vv instanceof List){
                t = NutsContentType.JSON;
            }
//        }else {
//            t = t == null ? NutsContentType.PLAIN : t;
//            if (getSession().isBot()) {
//                switch (t) {
//                    case PLAIN: {
//                        t = NutsContentType.PROPS;
//                        break;
//                    }
//                    case TREE:
//                    case TABLE: {
//                        t = NutsContentType.JSON;
//                        break;
//                    }
//                }
//            }
        }
        switch (t) {
            //structured formats!
            case XML:
            case JSON:
            case TSON:
            case YAML:
            {
                return ws.elem().setContentType(t);
            }
            case PROPS: {
                return ws.formats().props();
            }
            case TREE: {
                return ws.formats().tree();
            }
            case TABLE: {
                return ws.formats().table();
            }
            case PLAIN: {
                return new NutsObjectFormatPlain(ws.getWorkspace());
            }
        }
        throw new NutsUnsupportedEnumException(getSession(), t);
    }

//    @Override
//    public NutsSession getSession() {
//        return base != null ? base.getSession() : super.getSession();
//    }

    @Override
    public NutsString format() {
        return getBase().format();
    }

    @Override
    public void print(NutsPrintStream out) {
        getBase().print(out);
    }

    @Override
    public void print(Writer out) {
        getBase().print(out);
    }

    @Override
    public void print(Path out) {
        getBase().print(out);
    }

    @Override
    public void print(File out) {
        getBase().print(out);
    }

    @Override
    public void print() {
        getBase().print();
    }

    @Override
    public void print(NutsSessionTerminal terminal) {
        getBase().print(terminal);
    }

    @Override
    public void println(Writer w) {
        getBase().println(w);
    }

    @Override
    public void println(NutsPrintStream out) {
        getBase().println(out);
    }

    @Override
    public void println(Path path) {
        getBase().println(path);
    }

    @Override
    public void println() {
        getBase().println();
    }

    @Override
    public void println(NutsSessionTerminal terminal) {
        getBase().println(terminal);
    }

    @Override
    public void println(File file) {
        getBase().println(file);
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return getBase().configureFirst(commandLine);
    }
}
