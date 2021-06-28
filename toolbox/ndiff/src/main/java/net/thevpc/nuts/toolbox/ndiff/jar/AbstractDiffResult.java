/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndiff.jar;

import java.util.*;

/**
 * @author thevpc
 */
public class AbstractDiffResult implements DiffResult {

    private final DiffCommand diff;
    private final TreeMap<DiffKey, String> added;
    private final TreeMap<DiffKey, String> removed;
    private final TreeMap<DiffKey, String[]> changed;
    private DiffEvalContext diffEvalContext;

    public AbstractDiffResult(DiffCommand diff, DiffEvalContext diffEvalContext,TreeMap<DiffKey, String> added, TreeMap<DiffKey, String> removed, TreeMap<DiffKey, String[]> changed) {
        this.diff = diff;
        this.added = added;
        this.removed = removed;
        this.changed = changed;
        this.diffEvalContext = diffEvalContext;
    }

    public DiffEvalContext getDiffEvalContext() {
        return diffEvalContext;
    }

    public DiffCommand getDiff() {
        return diff;
    }



    @Override
    public Iterator iterator() {
        return all().iterator();
    }

    @Override
    public boolean hasChanges() {
        return !added.isEmpty()
                || !removed.isEmpty()
                || !changed.isEmpty();
    }



    @Override
    public List<DiffItem> all() {
        List<DiffItem> a = new ArrayList<>();
        for (Map.Entry<DiffKey, String> n : removed.entrySet()) {
            a.add(diff.createChildItem(n.getKey(), DiffStatus.REMOVED, n.getValue(), null, diffEvalContext));
        }
        for (Map.Entry<DiffKey, String[]> n : changed.entrySet()) {
            a.add(diff.createChildItem(n.getKey(), DiffStatus.CHANGED, n.getValue()[0], n.getValue()[1], diffEvalContext));
        }
        for (Map.Entry<DiffKey, String> n : added.entrySet()) {
            a.add(diff.createChildItem(n.getKey(), DiffStatus.ADDED, null, n.getValue(), diffEvalContext));
        }
        return a;
    }

    @Override
    public List<DiffItem> removed() {
        List<DiffItem> a = new ArrayList<>();
        for (Map.Entry<DiffKey, String> n : removed.entrySet()) {
            a.add(diff.createChildItem(n.getKey(), DiffStatus.REMOVED, n.getValue(), null, diffEvalContext));
        }
        return a;
    }

    @Override
    public List<DiffItem> added() {
        List<DiffItem> a = new ArrayList<>();
        for (Map.Entry<DiffKey, String> n : added.entrySet()) {
            a.add(diff.createChildItem(n.getKey(), DiffStatus.ADDED, null, n.getValue(), diffEvalContext));
        }
        return a;
    }

    @Override
    public List<DiffItem> changed() {
        List<DiffItem> a = new ArrayList<>();
        for (Map.Entry<DiffKey, String[]> n : changed.entrySet()) {
            a.add(diff.createChildItem(n.getKey(), DiffStatus.CHANGED, n.getValue()[0], n.getValue()[1], diffEvalContext));
        }
        return a;
    }



    @Override
    public void close() throws Exception {

    }

    @Override
    public String toString() {
        List<DiffItem> all = all();
        if(all.isEmpty()){
            return "<NO DIFFS>";
        }
        StringBuilder sb = new StringBuilder();
        print("",all,sb);
        return sb.toString();
    }

    private void print(String prefix,List<DiffItem> all,StringBuilder sb){
        for (DiffItem diffItem : all) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(prefix).append(diffItem.toString());
            List<DiffItem> c = diffItem.children();
            if(c.size()>0){
                print(prefix+"  ",c,sb);
            }
        }
    }
}
