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
    public boolean isLeaf(Object node) {
        return ((VNNote) node).getChildren().size() == 0;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((VNNote) parent).getChildren().indexOf(child);
    }

    public void treeStructureChanged() {
        fireTreeStructureChanged(this, getPathToRoot((VNNote) getRoot()), null, null);
    }

    public void nodeStructureChanged(VNNote node) {
        if (node != null) {
            fireTreeStructureChanged(this, getPathToRoot(node), null, null);
        }
    }

    public VNNote[] getPathToRoot(VNNote aNode) {
        return getPathToRoot(aNode, 0);
    }

    protected VNNote[] getPathToRoot(VNNote aNode, int depth) {
        VNNote[] retNodes;
        if (aNode == null) {
            if (depth == 0) {
                return null;
            } else {
                retNodes = new VNNote[depth];
            }
        } else {
            depth++;
            if (aNode == getRoot()) {
                retNodes = new VNNote[depth];
            } else {
                retNodes = getPathToRoot(aNode.getParent(), depth);
            }
            retNodes[retNodes.length - depth] = aNode;
        }
        return retNodes;
    }

    public void setRoot(VNNote copyFrom) {
        this.root = copyFrom;
        treeStructureChanged();

    }

    @Override
    protected void insertNodeIntoImpl(Object parent, Object newChild, int index) {
        ((VNNote) parent).addChild(index, ((VNNote) newChild));
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
    public Object copyNode(Object node) {
        return ((VNNote) node).copy();
    }

}
