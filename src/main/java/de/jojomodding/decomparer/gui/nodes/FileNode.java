package de.jojomodding.decomparer.gui.nodes;

import de.jojomodding.decomparer.Side;
import de.jojomodding.decomparer.processing.DiffedMapDifference;
import de.jojomodding.decomparer.processing.TreePath;

import javax.swing.plaf.nimbus.State;
import javax.swing.tree.TreeNode;
import java.nio.file.Path;
import java.util.Enumeration;

public class FileNode implements SidedNode {

    private FolderNode parent;
    private String name;
    private DiffedMapDifference.Status state;
    private TreePath p;

    public FileNode(String name, FolderNode parent, DiffedMapDifference.Status state, TreePath fullPath){
        this.name = name;
        this.parent = parent;
        this.state = state;
        this.p = fullPath;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public TreeNode getChildAt(int i) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode treeNode) {
        return 0;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public Enumeration<? extends TreeNode> children() {
        return new Enumeration<TreeNode>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public TreeNode nextElement() {
                return null;
            }
        };
    }

    public DiffedMapDifference.Status getSide() {
        return state;
    }

    public TreePath getPath(){
        return p;
    }
}
