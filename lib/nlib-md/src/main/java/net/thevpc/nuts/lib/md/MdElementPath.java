/**
 * ====================================================================
 *            thevpc-common-md : Simple Markdown Manipulation Library
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.lib.md;

/**
 *
 * @author thevpc
 */
public class MdElementPath<T extends MdElement>{

    public static final MdElementPath ROOT = new MdElementPath();
    private MdElementPath parent;
    private T element;

    private MdElementPath(T element) {
        this(element, null);
    }

    private MdElementPath() {
    }

    private MdElementPath(T element, MdElementPath parent) {
        if (element == null) {
            throw new NullPointerException();
        }
        if (this.parent!=null && parent.isRoot()) {
            parent=null;
        }
        this.element = element;
        this.parent = parent;
    }

    public boolean isRoot() {
        return parent == null && element==null;
    }
    
    public boolean isFirstLevel() {
        return parent == null && parent!=null;
    }

    public MdElementPath getParentPath() {
        return parent;
    }

    public T getElement() {
        return element;
    }

    public MdElementPath append(MdElement e) {
        if (this.element == null) {
            return new MdElementPath(e);
        }
        return new MdElementPath(e, this);
    }

    @Override
    public String toString() {
        String prefix="";
        if(parent!=null){
            prefix=parent.toString()+"/";
        }
        switch(element.getElementType()){
            case XML:{
                return prefix+"<"+element.asXml().getTag()+">";
            }
            case SEQ:{
                return prefix+String.valueOf(element.getElementType())+"[..."+element.asSeq().getElements().length+"]";
            }
            case CODE:{
                return prefix+String.valueOf(element.getElementType())+"("+element.asCode().getLanguage()+")";
            }
        }
        return prefix+String.valueOf(element.getElementType());
    }
    
}
