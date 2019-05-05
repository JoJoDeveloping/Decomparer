package de.jojomodding.decomparer.gui.difftextpane;


import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import de.jojomodding.decomparer.processing.DiffFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DiffTextArea extends JComponent implements ComponentListener {

    private JScrollBar bar;
    private int leftOffset;

    private List<ColorizedString> left, right;
    private int textlenmaxleft, texlenmaxright, textlenmax;

    public DiffTextArea(JScrollBar bar, String fontName){
        this.bar = bar;
        bar.addAdjustmentListener(adjustmentEvent -> {
            leftOffset = adjustmentEvent.getValue();
            repaint();
        });
        addComponentListener(this);
        setSize(200, 0);
        setFont(new Font(fontName, Font.PLAIN, 12));
        setForeground(Color.white);
        setBackground(new Color(64,64,64));
        setText(List.of(), new Patch<>());
    }

    public void setText(List<String> text, Patch<String> patch) {
        processText(text, patch);
        this.textlenmaxleft = left.stream().map(s -> getFontMetrics(getFont()).stringWidth(s.getBase())).max(Integer::compareTo).orElse(0);
        this.texlenmaxright = right.stream().map(s -> getFontMetrics(getFont()).stringWidth(s.getBase())).max(Integer::compareTo).orElse(0);
        textlenmax = Math.max(textlenmaxleft, texlenmaxright);
        bar.setMaximum(textlenmax);
        bar.setVisibleAmount(getWidth()/2-10);
        bar.setVisible(getWidth() < texlenmaxright+textlenmaxleft+18);
        bar.setValue(0);
        setMinimumSize(new Dimension(200, getFontMetrics(getFont()).getHeight()*text.size()+4));
        setPreferredSize(getMinimumSize());
        setSize(getSize().width, getMinimumSize().height);
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        Shape clip = g.getClip();
        g.setColor(getBackground());
        g.fillRect(0,0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.fillRect(getWidth()/2-5,0,10,getHeight());
        g.setColor(getForeground());
        int newOffset = getWidth()/2+5;
        g.clipRect(2,2,newOffset-14,getHeight()-4);
        int y = 2+getFontMetrics(getFont()).getAscent(), dy = getFontMetrics(getFont()).getHeight();
        for(ColorizedString s : left){
            drawColoredString(s, -leftOffset+2,y, g);
            y+=dy;
        }
        g.setClip(clip);
        g.clipRect(newOffset+2,2,newOffset-14,getHeight()-4);
        y = 2+getFontMetrics(getFont()).getAscent();
        for(ColorizedString s : right){
            drawColoredString(s, newOffset-leftOffset+2,y,g);
            y+=dy;
        }
    }

    @Override
    public void componentResized(ComponentEvent componentEvent) {
        bar.setMaximum(textlenmax);
        bar.setVisibleAmount(getWidth()/2-10);
        bar.setVisible(getWidth() < texlenmaxright+textlenmaxleft+18);
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent componentEvent) {

    }

    @Override
    public void componentShown(ComponentEvent componentEvent) {

    }

    @Override
    public void componentHidden(ComponentEvent componentEvent) {

    }

    private void drawColoredString(ColorizedString s, int x, int y, Graphics g){
        int ox = x;
        FontMetrics fm = g.getFontMetrics();
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            g.setColor(s.backgroundAt(i));
            int cw = fm.charWidth(c);
            g.fillRect(x,y-fm.getAscent(),cw,fm.getHeight());
            g.setColor(s.foregroundAt(i));
            g.drawString(""+c,x,y);
            x+=cw;
            if(c == '\n'){
                y+=fm.getHeight();
                x=ox;
            }
        }
    }

    private void processText(List<String> text, Patch<String> patch){
        Map.Entry<List<ColorizedString>, List<ColorizedString>> i = DiffFormatter.format(text, patch);
        this.left = i.getKey();
        this.right = i.getValue();
    }
}
