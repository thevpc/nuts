/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.util.common;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyJavascriptFilter;
import net.vpc.app.nuts.core.filters.id.NutsJavascriptIdFilter;
import net.vpc.app.nuts.core.filters.version.NutsVersionJavascriptFilter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.regex.Pattern;
import net.vpc.app.nuts.core.DefaultNutsVersion;

/**
 *
 * @author vpc
 */
public class JavascriptHelper {

    public List<Pattern> blacklistClassNamePatterns = new ArrayList<>();
    private ScriptEngine engine;
    private NutsWorkspace ws;

    private Set<String> blacklistClassNames = new HashSet<>(
            Arrays.asList(
                    "java.io.File",
                    "java.lang.Process",
                    "java.lang.System",
                    "java.lang.Thread"
            )
    );

    public static class NutScriptUtil {

        NutsWorkspace ws;
        NutsSession session;

        public NutScriptUtil(NutsWorkspace ws, NutsSession session) {
            this.ws = ws;
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
                return value.toString().matches(CoreStringUtils.simpexpToRegexp(pattern, true));
            }
            if (CoreStringUtils.isBlank(pattern)) {
                return CoreStringUtils.isBlank(value.toString());
            }
            if (value instanceof NutsId) {
                NutsJavascriptIdFilter f = NutsJavascriptIdFilter.valueOf(pattern);
                return f == null || f.accept((NutsId) value, ws, session);
            }
            if (value instanceof NutsDependency) {
                NutsDependencyJavascriptFilter f = NutsDependencyJavascriptFilter.valueOf(pattern);
                //TODO, how to pass parent Id for dependency?
                NutsId from = null;
                return f == null || f.accept(from, (NutsDependency) value, ws, session);
            }
            if (value instanceof NutsVersion) {
                NutsVersionJavascriptFilter f = NutsVersionJavascriptFilter.valueOf(pattern, ws);
                return f == null || f.accept((NutsVersion) value, ws, session);
            }
            return true;
        }

        public String trim(String s) {
            return CoreStringUtils.trim(s);
        }

        public int compareVersions(String v1, String v2) {
            return DefaultNutsVersion.compareVersions(v1, v2);
        }

    }

    public JavascriptHelper(String code, String initExprs, Set<String> blacklist, Object util, NutsWorkspace ws, NutsSession session) {
        this.ws = ws;
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
                    blacklistClassNamePatterns.add(Pattern.compile(CoreStringUtils.simpexpToRegexp(s)));
                } else {
                    blacklistClassNames.add(s);
                }
            }
        }
        if (code == null) {
            throw new NutsIllegalArgumentException(ws, "Illegal js filter : empty content");
        }
        if (!code.contains("return")) {
            throw new NutsIllegalArgumentException(ws, "js filter must contain a return clause");
        }
        try {
            engine = createScriptEngine();
        } catch (Exception ex) {
            engine = createManagerJdk();
        }
        try {
            if (CoreStringUtils.isBlank(initExprs)) {
                initExprs = "";
            }
            engine.eval("function accept(x) { " + initExprs + code + " }");
            if (util == null) {
                util = new NutScriptUtil(ws, session);
            }
            engine.put("util", util);
        } catch (ScriptException e) {
            throw new NutsParseException(ws, e);
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
            throw new NutsParseException(ws, e);
        }
    }
}
