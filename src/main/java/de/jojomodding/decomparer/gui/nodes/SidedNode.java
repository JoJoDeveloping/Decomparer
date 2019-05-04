package de.jojomodding.decomparer.gui.nodes;

import de.jojomodding.decomparer.processing.DiffedMapDifference;

import javax.swing.tree.TreeNode;

public interface SidedNode extends TreeNode {

    DiffedMapDifference.Status getSide();

}
