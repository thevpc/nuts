///**
// * ====================================================================
// *            Nuts : Network Updatable Things Service
// *                  (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages
// * and libraries for runtime execution. Nuts is the ultimate companion for
// * maven (and other build managers) as it helps installing all package
// * dependencies at runtime. Nuts is not tied to java and is a good choice
// * to share shell scripts and other 'things' . Its based on an extensible
// * architecture to help supporting a large range of sub managers / repositories.
// * <br>
// *
// * Copyright [2020] [thevpc]
// * Licensed under the Apache License, Version 2.0 (the "License"); you may
// * not use this file except in compliance with the License. You may obtain a
// * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// * either express or implied. See the License for the specific language
// * governing permissions and limitations under the License.
// * <br>
// * ====================================================================
//*/
//package net.thevpc.nuts.runtime.standalone.executors;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
//import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
//import net.thevpc.nuts.NutsExecutorComponent;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import net.thevpc.nuts.runtime.core.util.CoreBooleanUtils;
//
///**
// * Created by vpc on 1/7/17.
// */
//@NutsSingleton
//public class ShellExecutorComponent implements NutsExecutorComponent {
//
//    public static NutsId ID;
//    NutsWorkspace ws;
//
//    @Override
//    public NutsId getId() {
//        return ID;
//    }
//
//    @Override
//    public int getSupportLevel(NutsSupportLevelContext<NutsDefinition> nutsDefinition) {
//        this.ws=nutsDefinition.getWorkspace();
//        if(ID==null){
//            ID=ws.id().parser().parse("net.thevpc.nuts.exec:exec-nsh");
//        }
//        if ("nsh".equals(nutsDefinition.getConstraints().getDescriptor().getPackaging())
//                || "nuts".equals(nutsDefinition.getConstraints().getDescriptor().getPackaging())) {
//            return DEFAULT_SUPPORT + 1;
//        }
//        return NO_SUPPORT;
//    }
//
//    public void exec(NutsExecutionContext executionContext) {
//        execHelper(executionContext,false);
//    }
//
//    public void dryExec(NutsExecutionContext executionContext) {
//        execHelper(executionContext,true);
//    }
//
//    public void execHelper(NutsExecutionContext executionContext,boolean dry) {
//        NutsDefinition nutMainFile = executionContext.getDefinition();
//        String[] execArgs = executionContext.getExecutorArguments();
//        String[] appArgs = executionContext.getArguments();
//
//        String dir = null;
//        boolean showCommand = CoreBooleanUtils.getSysBoolNutsProperty("show-command", false);
//        for (int i = 0; i < execArgs.length; i++) {
//            String arg = execArgs[i];
//            if (arg.equals("--show-command") || arg.equals("-show-command")) {
//                showCommand = true;
//            } else if (arg.equals("--dir") || arg.equals("-dir")) {
//                i++;
//                dir = execArgs[i];
//            } else if (arg.startsWith("--dir=") || arg.startsWith("-dir=")) {
//                dir = execArgs[i].substring(arg.indexOf('=') + 1);
//            }
//        }
//
////        List<String> env = new ArrayList<>();
////        env.addAll(Arrays.asList(envAndApp0[0]));
////        env.addAll(Arrays.asList(envAndApp[0]));
//        List<String> app = new ArrayList<>();
//        app.add(NutsConstants.Ids.NUTS_SHELL);
//        app.add(nutMainFile.getPath().toString());
//        app.addAll(Arrays.asList(appArgs));
//
//        File directory = CoreStringUtils.isBlank(dir) ? null : new File(executionContext.getWorkspace().io().expandPath(dir));
//        executionContext.getWorkspace()
//                .exec()
//                .addCommand(app)
//                .setSession(executionContext.getExecSession())
//                .setEnv(executionContext.getEnv())
//                .setDirectory(directory == null ? null : directory.getPath())
//                .setFailFast(true)
//                //                .showCommand(showCommand)
//                .setExecutionType(executionContext.getExecutionType())
//                .setDry(dry)
//                .run();
//    }
//
//}
