package de.jojomodding.decomparer.processing;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import de.jojomodding.decomparer.gui.difftextpane.ColorizedString;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class FormattedDiff {

    private List<ColorizedString> left, right;
    private BitSet changedLines;

    public FormattedDiff(List<String> left, Patch<String> right){
        this.left = new ArrayList<>(left.size());
        this.right = new ArrayList<>(left.size());
        changedLines = new BitSet();
        if(right != null)try {
            List<DiffRow> dr = DiffRowGenerator.create().showInlineDiffs(true).oldTag(f -> "\0").newTag(f -> "\0").reportLinesUnchanged(true).build().generateDiffRows(left, right);
            for(int line = 0; line < dr.size(); line++){
                DiffRow r = dr.get(line);
                String[] leftSubs = r.getOldLine().split("\0", -1);
                ColorizedString cs = new ColorizedString(leftSubs[0], Color.white, new Color(64,64,64));
                for(int i = 1; i < leftSubs.length; i++){
                    cs.append(leftSubs[i], Color.white, new Color((i%2)==0?64:128,64,64));
                    changedLines.set(line);
                }
                this.left.add(cs);
                String[] rightSubs = r.getNewLine().split("\0", -1);
                cs = new ColorizedString(rightSubs[0], Color.white, new Color(64,64,64));
                for(int i = 1; i < rightSubs.length; i++){
                    cs.append(rightSubs[i], Color.white, new Color(64,(i%2)==0?64:128,64));
                    changedLines.set(line);
                }
                this.right.add(cs);
            }
            return;
        } catch (DiffException e) {
            e.printStackTrace();
        }
        this.left = List.of(new ColorizedString("Could not compute diff!", Color.white, Color.red));
        this.right = List.of(new ColorizedString("", Color.white, Color.red));
        changedLines = new BitSet(1);
    }

    public List<ColorizedString> getLeft() {
        return left;
    }

    public List<ColorizedString> getRight() {
        return right;
    }

    public BitSet getChangedLines() {
        return changedLines;
    }
}
