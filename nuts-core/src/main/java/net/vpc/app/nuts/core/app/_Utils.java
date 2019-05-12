/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author vpc
 */
class _Utils {

    public static String[] split(String str, String separators) {
        if (str == null) {
            return new String[0];
        }
        StringTokenizer st = new StringTokenizer(str, separators);
        List<String> result = new ArrayList<>();
        while (st.hasMoreElements()) {
            result.add(st.nextToken());
        }
        return result.toArray(new String[0]);
    }
}
