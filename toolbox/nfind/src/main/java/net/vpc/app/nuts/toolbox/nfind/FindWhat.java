/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nfind;

import java.util.HashSet;
import net.vpc.common.strings.StringUtils;

/**
 *
 * @author vpc
 */
class FindWhat {
    
    String jsCode = null;
    HashSet<String> nonjs = new HashSet<String>();
    private final NFindMain outer;

    FindWhat(final NFindMain outer) {
        this.outer = outer;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(jsCode)) {
            sb.append("js::'").append(jsCode).append('\'');
        }
        for (String v : nonjs) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append("'");
            sb.append(v);
            sb.append("'");
        }
        return sb.toString();
    }
    
}
