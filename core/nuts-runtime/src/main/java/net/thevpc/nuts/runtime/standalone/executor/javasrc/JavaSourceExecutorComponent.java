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
package net.thevpc.nuts.runtime.standalone.executor.javasrc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.runtime.standalone.executor.java.JavaExecutorComponent;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class JavaSourceExecutorComponent implements NExecutorComponent {

    public static NId ID;

    @Override
    public NId getId() {
        return ID;
    }

    @Override
    public int exec(NExecutionContext executionContext) {
        if(executionContext.isDry()){
            NDefinition nutMainFile = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
            Path javaFile = nutMainFile.getContent().flatMap(NPath::toPath).orNull();
            String folder = "__temp_folder";
            NPrintStream out = executionContext.getSession().out();
            out.println(NText.ofStyledPrimary4("compile"));
            out.println(
                    NCmdLine.of(
                            new String[]{
                                    "embedded-javac",
                                    "-d",
                                    "<temp-folder>",
                                    javaFile.toString()
                            }
                    )
            );
            JavaExecutorComponent cc = new JavaExecutorComponent();
            NDefinition d = executionContext.getDefinition();
            d = new DefaultNDefinition(d, executionContext.getWorkspace());
            ((DefaultNDefinition) d).setContent(
                    NPath.of(folder).setUserCache(false).setUserTemporary(true)
            );
            String fileName = javaFile.getFileName().toString();
            List<String> z = new ArrayList<>(executionContext.getExecutorOptions());
            z.addAll(Arrays.asList("--main-class",
                    new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                    "--class-path",
                    folder.toString()));
            NExecutionContext executionContext2 = NWorkspaceExt.of()
                    .createExecutionContext()
                    .setAll(executionContext)
                    .setDefinition(d)
                    .setExecutorOptions(z)
                    .failFast()
                    .temporary()
                    .build();
            return cc.exec(executionContext2);
        }else {
            NDefinition nutMainFile = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
            Path javaFile = nutMainFile.getContent().flatMap(NPath::toPath).orNull();
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            NSession session = executionContext.getSession();
            Path folder = NPath
                    .ofTempFolder("jj").toPath().get();
            int res = compiler.run(null, null, null, "-d", folder.toString(), javaFile.toString());
            if (res != NExecutionException.SUCCESS) {
                throw new NExecutionException(NMsg.ofPlain("compilation failed"), res);
            }
            JavaExecutorComponent cc = new JavaExecutorComponent();
            NDefinition d = executionContext.getDefinition();
            d = new DefaultNDefinition(d, executionContext.getWorkspace());
            ((DefaultNDefinition) d).setContent(NPath.of(folder).setUserCache(false).setUserTemporary(true));
            String fileName = javaFile.getFileName().toString();
            List<String> z = new ArrayList<>(executionContext.getExecutorOptions());
            z.addAll(Arrays.asList("--main-class",
                    new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                    "--class-path",
                    folder.toString()));
            NExecutionContext executionContext2 = NWorkspaceExt.of()
                    .createExecutionContext()
                    .setAll(executionContext)
                    .setDefinition(d)
                    .setExecutorOptions(z)
                    .failFast()
                    .temporary()
                    .build();
            return cc.exec(executionContext2);
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        if (ID == null) {
            ID = NId.get("net.thevpc.nuts.exec:exec-java-src").get();
        }
        NDefinition def = context.getConstraints(NDefinition.class);
        if (def != null) {
            if ("java".equals(def.getDescriptor().getPackaging())) {
                return NConstants.Support.DEFAULT_SUPPORT + 1;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

}
