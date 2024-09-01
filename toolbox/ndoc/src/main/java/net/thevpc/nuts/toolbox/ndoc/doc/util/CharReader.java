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
package net.thevpc.nuts.toolbox.ndoc.doc.util;

/**
 *
 * @author thevpc
 */
public class CharReader {

    private StringBuilder sb = new StringBuilder();

    public CharReader(String s) {
        sb.append(s);
    }

    public boolean isEmpty() {
        return sb.length() == 0;
    }

    public char peek() {
        return sb.charAt(0);
    }
    
    public char read() {
        char c = sb.charAt(0);
        sb.delete(0, 1);
        return c;
    }

    public boolean read(String z) {
        if (peek(z)) {
            sb.delete(0, z.length());
            return true;
        }
        return false;
    }

    public boolean peek(String z) {
        if (z == null || z.length() == 0) {
            throw new IllegalArgumentException("empty peek");
        }
        return peek(z.toCharArray());
    }

//    public String readUntil(char[] z) {
//        int i = indexOf(z, 0);
//        if(i<0){
//            
//        }
//    }
    
    public int indexOf(char[] z,int fromIndex) {
        for (int i = fromIndex; i < sb.length()-z.length+1; i++) {
            if(peek(z)){
                return i;
            }
        }
        return -1;
    }
    
    public boolean read(char[] z) {
        if (peek(z)) {
            sb.delete(0, z.length);
            return true;
        }
        return false;
    }

    public boolean peek(char[] z) {
        if (z == null || z.length == 0) {
            throw new IllegalArgumentException("empty peek");
        }
        if (sb.length() >= z.length) {
            for (int i = 0; i < z.length; i++) {
                if (sb.charAt(i) != z[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean peek(char[] z,int fromIndex) {
        if (z == null || z.length == 0) {
            throw new IllegalArgumentException("empty peek");
        }
        if (sb.length()-fromIndex >= z.length) {
            for (int i = 0; i < z.length; i++) {
                if (sb.charAt(i+fromIndex) != z[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean peek(char z) {
        if (sb.length() >= 1) {
            return sb.charAt(0) == z;
        }
        return false;
    }
}
