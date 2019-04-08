/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.bundledlibs.util;

import java.io.File;
import java.util.Iterator;
import java.util.Stack;

/**
 *
 * @author vpc
 */
public class FileDepthFirstIterator implements Iterator<File> {

    private final Stack<File> stack = new Stack<>();
    private File lastFolder = null;
    private File lastFile = null;

    public FileDepthFirstIterator(File file) {
        if (file != null) {
            stack.push(file);
        }
    }

    /**
     * this method can be called immediately after next() to disable current
     * folder's children to be processed
     */
    public void dropChildren() {
        this.lastFolder = null;
    }

    @Override
    public boolean hasNext() {
        if (this.lastFolder != null) {
            File[] ch = lastFolder.listFiles();
            this.lastFolder = null;
            if (ch != null) {
                for (File file : ch) {
                    stack.push(file);
                }
            }

        }
        return !stack.isEmpty();
    }

    @Override
    public File next() {
        lastFile = stack.pop();
        if (lastFile.isDirectory()) {
            this.lastFolder = lastFile;
        }
        return lastFile;
    }

    @Override
    public void remove() {
        lastFile.delete();
    }

}
