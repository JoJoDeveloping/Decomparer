package de.jojomodding.decomparer.gui;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import de.jojomodding.decomparer.Side;
import de.jojomodding.decomparer.gui.nodes.DiffTreeCellRenderer;
import de.jojomodding.decomparer.gui.nodes.FileNode;
import de.jojomodding.decomparer.gui.nodes.FolderNode;
import de.jojomodding.decomparer.processing.DifferentialFileTree;
import de.jojomodding.decomparer.processing.TreePath;
import org.eclipse.jgit.diff.DiffFormatter;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DecomparerWindow {
    private JTree filetree;
    private JTextArea diffview;
    public JPanel panel;
    private DifferentialFileTree dft;

    public DecomparerWindow(DifferentialFileTree dft){
        this.dft = dft;
        filetree.addTreeSelectionListener(treeSelectionEvent -> {
            TreeNode tn = (TreeNode) treeSelectionEvent.getPath().getLastPathComponent();
            if(tn instanceof FileNode){
                StringBuilder sb = new StringBuilder();
                Map.Entry<List<String>, Patch<String>> k = dft.find(((FileNode) tn).getPath());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    List<DiffRow> lines = DiffRowGenerator.create().showInlineDiffs(true).mergeOriginalRevised(false).columnWidth(160).newTag(f -> "**").oldTag(f -> "~~").build().generateDiffRows(k.getKey(), k.getValue());
                    int len = lines.stream().map(DiffRow::getOldLine).map(String::length).max(Integer::compare).orElse(0);
                    char [] wd = new char[len];
                    Arrays.fill(wd, ' ');
                    String white = new String(wd)+" | ";
                    for(DiffRow d : lines){
                        sb.append(d.getOldLine()).append(white.substring(d.getOldLine().length())).append(d.getNewLine()).append('\n');
                    }
                } catch (DiffException e) {
                    sb.append("Error diffing: \n").append(e.getMessage()).append('\n');
                    e.printStackTrace();
                }
                diffview.setText(sb.toString());
            }
        });
    }

    private void createUIComponents() {
        filetree = new JTree(new FolderNode(dft, null, new TreePath()));
        filetree.setCellRenderer(new DiffTreeCellRenderer());
    }
}
