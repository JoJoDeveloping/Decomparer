package de.jojomodding.decomparer.gui.difftextpane;

import java.awt.*;
import java.util.Arrays;

public class ColorizedString {

    private String s;
    private Color[] fg, bg;

    public ColorizedString(String base, Color fg, Color bg){
        this.s = base;
        this.fg = new Color[base.length()];
        this.bg = new Color[base.length()];
        Arrays.fill(this.fg, fg);
        Arrays.fill(this.bg, bg);
    }

    public int length(){
        return s.length();
    }

    public char charAt(int i){
        return s.charAt(i);
    }

    public Color foregroundAt(int i){
        if(fg[i] == null)
        return Color.BLUE;
        return fg[i];
    }

    public Color backgroundAt(int i){
        if(bg[i]==null)
        return Color.   BLACK;
        return bg[i];
    }

    public String getBase() {
        return s;
    }

    public void append(String s, Color fg, Color bg) {
        int os = this.s.length(), ns = s.length();
        this.s += s;
        Color[] nfg = new Color[length()], nbg = new Color[length()];
        System.arraycopy(this.fg, 0, nfg, 0, this.fg.length);
        Arrays.fill(nfg, this.fg.length, nfg.length, fg);
        System.arraycopy(this.bg, 0, nbg, 0, this.bg.length);
        Arrays.fill(nbg, this.bg.length, nbg.length, bg);
        this.fg = nfg;
        this.bg = nbg;
    }
}
