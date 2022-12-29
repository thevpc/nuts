package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.util.NStringUtils;

public class NDependencySolverUtils {

    public static final String DEFAULT_SOLVER_NAME = "maven";

    public static String resolveSolverName(String name){
        if(name==null){
            return DEFAULT_SOLVER_NAME;
        }
        name= NStringUtils.trim(name);
        String lcName=name.toLowerCase();
        if(lcName.equals("default")){
            lcName="maven";
        }
        //
        return lcName;
    }
}
