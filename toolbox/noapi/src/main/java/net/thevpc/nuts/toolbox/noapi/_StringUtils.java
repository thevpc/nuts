package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.nuts.NutsBlankable;

public class _StringUtils {
    public static String trim(String a){
        return a==null?"":a.trim();
    }
    public static String nvl(String ... a){
        if(a!=null){
            for (String s : a) {
                if(!NutsBlankable.isBlank(s)){
                    return trim(s);
                }
            }
        }
        return null;
    }
}
