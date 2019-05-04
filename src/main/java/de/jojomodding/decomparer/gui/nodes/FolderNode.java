package de.jojomodding.decomparer.gui.nodes;

import com.sun.source.tree.Tree;
import de.jojomodding.decomparer.Side;
import de.jojomodding.decomparer.processing.DiffedMapDifference;
import de.jojomodding.decomparer.processing.DifferentialFileTree;
import de.jojomodding.decomparer.processing.TreePath;

import javax.swing.tree.TreeNode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class FolderNode implements SidedNode {

    private String name;
    private DiffedMapDifference.Status on;
    private FolderNode parent;
    private List<SidedNode> children;

    public FolderNode(DifferentialFileTree dft, FolderNode parent, TreePath path){
        name = dft.getName();
        this.parent = parent;
        children = new ArrayList<>();
        dft.getSubdirs().stream().
                sorted(String::compareToIgnoreCase).
                map(dft::getSubdir).
                map(e -> new FolderNode(e, this, path.resolve(e.getName()))).
                forEach(children::add);
        dft.getFiles().getKeys().stream().sorted(String::compareToIgnoreCase).map(s -> new FileNode(s, this, dft.getFiles().describe(s), path.resolve(s))).forEach(children::add);
        on = children.stream().map(SidedNode::getSide).reduce((l,r) -> l==r ? l : DiffedMapDifference.Status.UNEQUAL).orElse(DiffedMapDifference.Status.EQUAL);
    }

    @Override
    public TreeNode getChildAt(int i) {
        return children.get(i);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode treeNode) {
        return children.indexOf(treeNode);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public Enumeration<? extends TreeNode> children() {
        return new Enumeration<TreeNode>() {
            int i = 0;

            @Override
            public boolean hasMoreElements() {
                return i < children.size();
            }

            @Override
            public TreeNode nextElement() {
                return children.get(i++);
            }
        };
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public DiffedMapDifference.Status getSide() {
        return on;
    }
}
