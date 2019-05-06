package de.jojomodding.decomparer.gui;

import com.github.difflib.patch.Patch;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.jojomodding.decomparer.config.Configuration;
import de.jojomodding.decomparer.gui.difftextpane.DiffTextArea;
import de.jojomodding.decomparer.gui.nodes.DiffTreeCellRenderer;
import de.jojomodding.decomparer.gui.nodes.FileNode;
import de.jojomodding.decomparer.gui.nodes.FolderNode;
import de.jojomodding.decomparer.processing.DifferentialFileTree;
import de.jojomodding.decomparer.processing.TreePath;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DecomparerWindow {
    private JTree filetree;
    private JPanel panel;
    private JSplitPane split;
    private JScrollBar scroller;
    private JScrollPane container;
    private JScrollPane filetreeContainer;
    private DiffTextArea textarea;
    private DifferentialFileTree dft;

    public DecomparerWindow(DifferentialFileTree dft, Configuration conf) {
        this.dft = dft;
        $$$setupUI$$$();
        filetree.addTreeSelectionListener(treeSelectionEvent -> {
            TreeNode tn = (TreeNode) treeSelectionEvent.getPath().getLastPathComponent();
            if (tn instanceof FileNode) {
                Map.Entry<List<String>, Patch<String>> diff = dft.find(((FileNode) tn).getPath());
                textarea.setText(diff.getKey(), diff.getValue());
            }
        });
        textarea = new DiffTextArea(scroller, conf.getFontName());
        container.setViewportView(textarea);
        container.getVerticalScrollBar().setUnitIncrement(16);
        split.setDividerLocation(0.3);
    }


    private void createUIComponents() {
        filetree = new JTree(new FolderNode(dft, null, new TreePath()));
        filetree.setCellRenderer(new DiffTreeCellRenderer());
        filetree.setMinimumSize(new Dimension(100, ~1));
        filetree.setSize(100, filetree.getHeight());
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel.setBackground(new Color(-12566464));
        panel.setForeground(new Color(-1));
        split = new JSplitPane();
        split.setBackground(new Color(-12040120));
        split.setContinuousLayout(true);
        split.setDividerLocation(150);
        split.setForeground(new Color(-12040120));
        split.setResizeWeight(0.3);
        panel.add(split, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(200, -1), new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        split.setRightComponent(panel1);
        container = new JScrollPane();
        container.setHorizontalScrollBarPolicy(31);
        container.setVerticalScrollBarPolicy(20);
        panel1.add(container, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(200, -1), null, null, 0, false));
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        container.setViewportView(toolBar$Separator1);
        scroller = new JScrollBar();
        scroller.setOrientation(0);
        panel1.add(scroller, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filetreeContainer = new JScrollPane();
        filetreeContainer.setVerticalScrollBarPolicy(20);
        split.setLeftComponent(filetreeContainer);
        filetree.setBackground(new Color(-12566464));
        filetree.setDropMode(DropMode.ON);
        filetree.setForeground(new Color(-1));
        filetree.setRootVisible(true);
        filetree.setShowsRootHandles(false);
        filetree.putClientProperty("JTree.lineStyle", "Angled");
        filetreeContainer.setViewportView(filetree);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }

}
