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
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.util.StringUtils;
import net.vpc.app.nuts.util.VersionUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsDescriptorJavascriptFilter implements NutsDescriptorFilter {
    private static NutsId SAMPLE_NUTS_ID = new NutsId("sample", "sample", "sample", "sample", "sample");
    private static DefaultNutsDescriptor SAMPLE_NUTS_DESCRIPTOR = new DefaultNutsDescriptor(
            SAMPLE_NUTS_ID,"default",
            new NutsId[]{SAMPLE_NUTS_ID},
            "sample",
            true,
            "sample",
            new NutsExecutorDescriptor(
                    SAMPLE_NUTS_ID,
                    new String[]{"sample"},
                    null
            ),
            new NutsExecutorDescriptor(
                    SAMPLE_NUTS_ID,
                    new String[]{"sample"},
                    null
            ),
            "sample",
            "sample",
            new String[]{"sample"},
            new String[]{"sample"},
            new String[]{"sample"},
            new String[]{"sample"},
            null,
            null
    );

    public Set<String> blacklistClassNames = new HashSet<>(
            Arrays.asList(
                    "java.io.File",
                    "java.lang.Process",
                    "java.lang.System",
                    "java.lang.Thread"
            )
    );
    public List<Pattern> blacklistClassNamePatterns = new ArrayList<>();
    private String code;
    private ScriptEngine engine;

    public NutsDescriptorJavascriptFilter(String code) {
        this(code, null);
    }

    public NutsDescriptorJavascriptFilter(String code, Set<String> blacklist) {
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
                    blacklistClassNamePatterns.add(Pattern.compile(StringUtils.simpexpToRegexp(s, false)));
                } else {
                    blacklistClassNames.add(s);
                }
            }
        }
        this.code = code;
        if (code == null) {
            throw new IllegalArgumentException("Illegal js filter : empty content");
        }
        if (!code.contains("return")) {
            throw new IllegalArgumentException("js filter must contain a return clause");
        }
        try {
            engine = createScriptEngine();
        } catch (Exception ex) {
            engine = createManagerJdk();
        }
        try {
            engine.eval("function accept(x) { var id=x.getId(); " + code + " }");
            engine.put("util", new NutScriptUtil());
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }

        //check if valid
        accept(SAMPLE_NUTS_DESCRIPTOR);
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

    public String getCode() {
        return code;
    }

    public boolean accept(NutsDescriptor d) {
        engine.put("d", d);
        try {
            return Boolean.TRUE.equals(engine.eval("accept(d);"));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public class NutScriptUtil {

        public boolean matches(String pattern, String value) {
            if (pattern == null) {
                pattern = "";
            }
            if (value == null) {
                value = "";
            }
            return value.matches(StringUtils.simpexpToRegexp(pattern, true));
        }

        public String trim(String s) {
            return StringUtils.trim(s);
        }

        public int compareVersions(String v1, String v2) {
            return VersionUtils.compareVersions(v1, v2);
        }

    }

}
