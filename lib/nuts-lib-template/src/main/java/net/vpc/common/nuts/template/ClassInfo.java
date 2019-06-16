/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.common.nuts.template;

/**
 *
 * @author vpc
 */
public class ClassInfo {
    
    String className;
    String packageName;

    public ClassInfo(String className, String packageName) {
        this.className = className;
        this.packageName = packageName;
    }

    public String getFullClassName() {
        if(packageName==null){
            return packageName;
        }
        return packageName+"."+className;
    }
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
}
