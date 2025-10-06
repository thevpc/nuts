/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.format.json;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.expr.NParseException;

public class JsonStatus {

    public int countBraces;
    public int openBraces;
    public int openBrackets;
    public boolean openAntiSlash;
    public boolean openSimpleQuotes;
    public boolean openDoubleQuotes;

    public JsonStatus() {
    }

    public boolean checkValid(boolean throwError) {
        if (!checkPartialValid(throwError)) {
            return false;
        }
        if (countBraces == 0) {
            if (throwError) {
                throw new NParseException(NMsg.ofPlain("not an object"));
            }
            return false;
        }
        if (openBrackets > 0) {
            if (throwError) {
                throw new NParseException(NMsg.ofPlain("unbalanced brackets"));
            }
            return false;
        }
        if (openBraces > 0) {
            if (throwError) {
                throw new NParseException(NMsg.ofPlain("unbalanced braces"));
            }
            return false;
        }
        if (openAntiSlash) {
            if (throwError) {
                throw new NParseException(NMsg.ofPlain("unbalanced anti-slash"));
            }
        }
        if (openSimpleQuotes) {
            if (throwError) {
                throw new NParseException(NMsg.ofPlain("unbalanced simple quotes"));
            }
            return false;
        }
        if (openDoubleQuotes) {
            if (throwError) {
                throw new NParseException(NMsg.ofPlain("unbalanced double quotes"));
            }
            return false;
        }
        return true;
    }

    public boolean checkPartialValid(boolean throwError) {
        if (openBrackets < 0) {
            if (throwError) {
                throw new NParseException(NMsg.ofPlain("unbalanced brackets"));
            }
            return false;
        }
        if (openBraces < 0) {
            if (throwError) {
                throw new NParseException(NMsg.ofPlain("unbalanced braces"));
            }
            return false;
        }
        return true;
    }
}
