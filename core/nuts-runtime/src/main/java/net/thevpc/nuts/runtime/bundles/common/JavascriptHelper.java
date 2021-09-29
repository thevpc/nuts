/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
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
package net.thevpc.nuts.runtime.bundles.common;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.string.GlobUtils;
import net.thevpc.nuts.runtime.core.model.DefaultNutsVersion;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.regex.Pattern;

/**
 *
 * @author thevpc
 */
public class JavascriptHelper {

    public List<Pattern> blacklistClassNamePatterns = new ArrayList<>();
    private ScriptEngine engine;
    private final NutsSession session;

    private final Set<String> blacklistClassNames = new HashSet<>(
            Arrays.asList(
                    "java.io.File",
                    "java.lang.Process",
                    "java.lang.System",
                    "java.lang.Thread"
            )
    );

    public JavascriptHelper(String code, String initExprs, Set<String> blacklist, Object util, NutsSession session) {
        this.session = session;
        if (blacklist == null) {
            blacklistClassNames.addAll(Arrays.asList(
                    "java.io.File",
                    "java.lang.Process",
                    "java.lang.System",
                    "java.lang.Thread"
            ));
        } else {
            for (String s : blacklist) {
                if (s.contains("*")) {
                    blacklistClassNamePatterns.add(GlobUtils.ofExact(s));
                } else {
                    blacklistClassNames.add(s);
                }
            }
        }
        if (code == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("illegal js filter : empty content"));
        }
        if (!code.contains("return")) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("js filter must contain a return clause"));
        }
        try {
            engine = createScriptEngine();
        } catch (Exception ex) {
            engine = createManagerJdk();
        }
        try {
            if (NutsBlankable.isBlank(initExprs)) {
                initExprs = "";
            }
            engine.eval("function accept(x) { " + initExprs + code + " }");
            if (util == null) {
                util = new NutScriptUtil(session);
            }
            engine.put("util", util);
        } catch (ScriptException e) {
            throw new NutsParseException(session, NutsMessage.plain("javascript execution failed"), e);
        }
    }

    public void createEngine(String code, String initExprs) {

    }

    private ScriptEngine createScriptEngine() {
        jdk.nashorn.api.scripting.NashornScriptEngineFactory f = new jdk.nashorn.api.scripting.NashornScriptEngineFactory();
        return f.getScriptEngine(new jdk.nashorn.api.scripting.ClassFilter() {
            @Override
            public boolean exposeToScripts(String s) {
                if (blacklistClassNames.contains(s)) {
                    return false;
                }
                for (Pattern pattern : blacklistClassNamePatterns) {
                    if (pattern.matcher(s).matches()) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    private ScriptEngine createManagerJdk() {
        ScriptEngineManager engineManager
                = new ScriptEngineManager();
        return engineManager.getEngineByName("nashorn");
    }

    public boolean accept(Object id) {
        engine.put("x", id);
        try {
            return Boolean.TRUE.equals(engine.eval("accept(x);"));
        } catch (ScriptException e) {
            throw new NutsParseException(session, NutsMessage.plain("javascript execution failed"), e);
        }
    }

    public static class NutScriptUtil {

        NutsSession session;

        public NutScriptUtil(NutsSession session) {
            this.session = session;
        }

        public boolean matches(Object value, String pattern) {
            if (value == null) {
                value = "";
            }
            if (value instanceof String) {
                if (pattern == null) {
                    pattern = "";
                }
                return GlobUtils.ofContains(pattern).matcher(value.toString()).matches();
            }
            if (NutsBlankable.isBlank(pattern)) {
                return NutsBlankable.isBlank(value.toString());
            }
            if (value instanceof NutsId) {
                NutsIdFilter f = session.id().filter().byExpression(pattern);
                return f.acceptId((NutsId) value, session);
            }
            if (value instanceof NutsDependency) {
                NutsDependencyFilter f = session.dependency().filter().byExpression(pattern);
                //TODO, how to pass parent Id for dependency?
                NutsId from = null;
                return f == null || f.acceptDependency(from, (NutsDependency) value, session);
            }
            if (value instanceof NutsVersion) {
                NutsVersionFilter f = session.version().filter().byExpression(pattern);
                return f == null || f.acceptVersion((NutsVersion) value, session);
            }
            return true;
        }

        public int compareVersions(String v1, String v2) {
            return DefaultNutsVersion.compareVersions(v1, v2);
        }

    }
}
