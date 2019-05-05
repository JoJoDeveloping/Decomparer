package de.jojomodding.decomparer.processing;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import de.jojomodding.decomparer.gui.difftextpane.ColorizedString;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiffFormatter {

    public static Map.Entry<List<ColorizedString>, List<ColorizedString>> format(List<String> left, Patch<String> right){
        if(right != null)try {
            List<ColorizedString> resl = new ArrayList<>(left.size()), resr = new ArrayList<>(left.size());
            List<DiffRow> dr = DiffRowGenerator.create().showInlineDiffs(true).oldTag(f -> "\0").newTag(f -> "\0").reportLinesUnchanged(true).build().generateDiffRows(left, right);
            for(DiffRow r : dr){
                String[] leftSubs = r.getOldLine().split("\0", -1);
                ColorizedString cs = new ColorizedString(leftSubs[0], Color.white, new Color(64,64,64));
                for(int i = 1; i < leftSubs.length; i++){
                    cs.append(leftSubs[i], Color.white, new Color((i%2)==0?64:128,64,64));
                }
                resl.add(cs);
                String[] rightSubs = r.getNewLine().split("\0", -1);
                cs = new ColorizedString(rightSubs[0], Color.white, new Color(64,64,64));
                for(int i = 1; i < rightSubs.length; i++){
                    cs.append(rightSubs[i], Color.white, new Color(64,(i%2)==0?64:128,64));
                }
                resr.add(cs);
            }
            return Map.entry(resl, resr);
        } catch (DiffException e) {
            e.printStackTrace();
        }
        return Map.entry(List.of(new ColorizedString("Could not compute diff!", Color.white, Color.red)), List.of());
    }

}
