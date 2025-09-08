/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.reserved.parser.NReservedVersionIntervalParser;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;
import java.util.List;

/**
 * @author thevpc
 * @app.category Base
 * @since 0.5.4
 */
public interface NVersionInterval extends Serializable {

    static NOptional<NVersionInterval> of(String s,NVersionComparator versionComparator) {
        return ofList(s,versionComparator).flatMap(
                x->{
                    if(x.isEmpty()){
                        return NOptional.ofEmpty(()-> NMsg.ofPlain("empty interval"));
                    }
                    if(x.size()>1){
                        return NOptional.ofError(()-> NMsg.ofPlain("too many intervals"));
                    }
                    return NOptional.of(x.get(0));
                }
        );
    }
    static NOptional<List<NVersionInterval>> ofList(String s,NVersionComparator versionComparator){
        return new NReservedVersionIntervalParser(versionComparator).parse(s);
    }
    boolean acceptVersion(NVersion version);

    boolean isFixedValue();

    boolean isIncludeLowerBound();

    boolean isIncludeUpperBound();

    String getLowerBound();

    String getUpperBound();
}
