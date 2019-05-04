package de.jojomodding.decomparer.gui.nodes;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class DiffTreeCellRenderer extends DefaultTreeCellRenderer {

    private Color bg;

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if(value instanceof SidedNode){
            SidedNode fn = (SidedNode) value;
            switch (fn.getSide()){
                case RIGHT:
                    bg = new Color(64, 128, 64);
                    break;
                case LEFT:
                    bg = new Color(128,64,64);
                    break;
                case UNEQUAL:
                    bg = new Color(64,64,128);
                    break;
                default:
                    bg = new Color(64,64,64);
                    break;
            }
        }
        setForeground(Color.WHITE);
        return c;
    }

    @Override
    public Color getBackground() {
        return bg;
    }

    @Override
    public Color getBackgroundNonSelectionColor() {
        return bg;
    }

    @Override
    public Color getBackgroundSelectionColor() {
        return bg.brighter();
    }
}
