/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.util.NStringUtils;

import java.time.Duration;

/**
 *
 * @author thevpc
 */
public class CoreTimeUtils {

    public static String formatPeriodMilli(Duration duration) {
        return duration.toString();
    }
    public static String formatPeriodMilli(long period) {
        StringBuilder sb = new StringBuilder();
        boolean started = false;
        int h = (int) (period / (1000L * 60L * 60L));
        int mn = (int) ((period % (1000L * 60L * 60L)) / 60000L);
        int s = (int) ((period % 60000L) / 1000L);
        int ms = (int) (period % 1000L);
        if (h > 0) {
            sb.append(NStringUtils.formatAlign(String.valueOf(h), 2, NPositionType.LAST)).append("h ");
            started = true;
        }
        if (mn > 0 || started) {
            sb.append(NStringUtils.formatAlign(String.valueOf(mn), 2, NPositionType.LAST)).append("mn ");
            started = true;
        }
        if (s > 0 || started) {
            sb.append(NStringUtils.formatAlign(String.valueOf(s), 2, NPositionType.LAST)).append("s ");
            //started=true;
        }
        sb.append(NStringUtils.formatAlign(String.valueOf(ms), 3, NPositionType.LAST)).append("ms");
        return sb.toString();
    }
    
}
