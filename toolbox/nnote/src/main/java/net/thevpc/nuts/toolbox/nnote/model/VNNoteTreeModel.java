/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.model;

import net.thevpc.common.swing.tree.AbstractTreeModel;

/**
 *
 * @author vpc
 */
public class VNNoteTreeModel extends AbstractTreeModel {

    private VNNote root;

    public VNNoteTreeModel(VNNote root) {
        this.root = root;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((VNNote) parent).getChildren().get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((VNNote) parent).getChildren().size();
    }

    @Override
    public boolean isLeaf(Object note) {
        return ((VNNote) note).getChildren().size() == 0;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((VNNote) parent).getChildren().indexOf(child);
    }

    public void treeStructureChanged() {
        fireTreeStructureChanged(this, getPathToRoot((VNNote) getRoot()), null, null);
    }

    public void nodeStructureChanged(VNNote note) {
        if (note != null) {
            fireTreeStructureChanged(this, getPathToRoot(note), null, null);
        }
    }

    public VNNote[] getPathToRoot(VNNote note) {
        return getPathToRoot(note, 0);
    }

    protected VNNote[] getPathToRoot(VNNote note, int depth) {
        VNNote[] retNotes;
        if (note == null) {
            if (depth == 0) {
                return null;
            } else {
                retNotes = new VNNote[depth];
            }
        } else {
            depth++;
            if (note == getRoot()) {
                retNotes = new VNNote[depth];
            } else {
                retNotes = getPathToRoot(note.getParent(), depth);
            }
            retNotes[retNotes.length - depth] = note;
        }
        return retNotes;
    }

    public void setRoot(VNNote copyFrom) {
        this.root = copyFrom;
        treeStructureChanged();

    }

    @Override
    protected void insertNodeIntoImpl(Object parent, Object newChild, int index) {
        if (newChild instanceof VNNote) {
            ((VNNote) parent).addChild(index, ((VNNote) newChild));
        } else if (newChild instanceof String) {
            ((VNNote) parent).addChild(index, VNNote.of(new NNote().setName((String)newChild)));
        }
    }

    @Override
    protected void removeNodeFromParentImpl(Object parent, int childIndex) {
        if (childIndex >= 0) {
            ((VNNote) parent).removeChild(childIndex);
        }
    }

    @Override
    public Object getParent(Object target) {
        return ((VNNote) target).getParent();
    }

    @Override
    public Object copyNode(Object note) {
        return ((VNNote) note).copy();
    }

}
