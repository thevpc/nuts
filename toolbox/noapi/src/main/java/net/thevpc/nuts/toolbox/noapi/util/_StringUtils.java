package net.thevpc.nuts.toolbox.noapi.util;

import net.thevpc.nuts.util.NBlankable;

public class _StringUtils {
    public static String trim(String a){
        return a==null?"":a.trim();
    }
    public static String nvl(String ... a){
        if(a!=null){
            for (String s : a) {
                if(!NBlankable.isBlank(s)){
                    return trim(s);
                }
            }
        }
        return null;
    }
}
