package de.jojomodding.decomparer.gui;

import com.github.difflib.patch.Patch;
import de.jojomodding.decomparer.config.Configuration;
import de.jojomodding.decomparer.gui.difftextpane.DiffTextArea;
import de.jojomodding.decomparer.gui.nodes.DiffTreeCellRenderer;
import de.jojomodding.decomparer.gui.nodes.FileNode;
import de.jojomodding.decomparer.gui.nodes.FolderNode;
import de.jojomodding.decomparer.processing.DifferentialFileTree;
import de.jojomodding.decomparer.processing.TreePath;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.List;
import java.util.Map;

public class DecomparerWindow {
    private JTree filetree;
    public JPanel panel;
    private JSplitPane split;
    private JScrollBar scroller;
    private JScrollPane container;
    private DiffTextArea textarea;
    private DifferentialFileTree dft;

    public DecomparerWindow(DifferentialFileTree dft, Configuration conf){
        this.dft = dft;
        filetree.addTreeSelectionListener(treeSelectionEvent -> {
            TreeNode tn = (TreeNode) treeSelectionEvent.getPath().getLastPathComponent();
            if(tn instanceof FileNode){
                Map.Entry<List<String>, Patch<String>> diff = dft.find(((FileNode) tn).getPath());
                textarea.setText(diff.getKey(), diff.getValue());
            }
        });
        textarea = new DiffTextArea(scroller, conf.getFontName());
        container.setViewportView(textarea);
        container.getVerticalScrollBar().setUnitIncrement(16);
        split.resetToPreferredSizes();
        split.invalidate();
        split.setDividerLocation(0.3);
    }



    private void createUIComponents() {
        filetree = new JTree(new FolderNode(dft, null, new TreePath()));
        filetree.setCellRenderer(new DiffTreeCellRenderer());
    }
}
