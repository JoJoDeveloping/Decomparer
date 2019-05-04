package de.jojomodding.decomparer.processing;

import com.github.difflib.patch.Patch;
import de.jojomodding.decomparer.Side;

import javax.swing.tree.TreeNode;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class DifferentialFileTree{
    private String name;
    private Map<String, DifferentialFileTree> onlyLeft;
    private Map<String, DifferentialFileTree> onlyRight;
    private Map<String, DifferentialFileTree> common;
    private DiffedMapDifference<String, String> files;

    protected DifferentialFileTree(String name){
        this.name = name;
        onlyLeft =  new HashMap<>();
        onlyRight = new HashMap<>();
        common = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Set<String> getSubdirs() {
        Set<String> s = new HashSet<>();
        s.addAll(onlyLeft.keySet());
        s.addAll(onlyRight.keySet());
        s.addAll(common.keySet());
        return s;
    }

    public DiffedMapDifference<String, String> getFiles() {
        return files;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void putLeftOnlySubdir(String name, DifferentialFileTree dft) {
        onlyLeft.put(name, dft);
    }

    protected void putRightOnlySubdir(String name, DifferentialFileTree dft) {
        onlyRight.put(name, dft);
    }

    protected void putCommonSubdir(String name, DifferentialFileTree dft) {
        common.put(name, dft);
    }

    public void setFiles(DiffedMapDifference dfd) {
        this.files = dfd;
    }

    public DifferentialFileTree getSubdir(String s) {
        DifferentialFileTree res = common.get(s);
        if(res == null)
            res = onlyRight.get(s);
        if(res == null)
            res = onlyLeft.get(s);
        return res;
    }

    public Map.Entry<List<String>, Patch<String>> find(TreePath path) {
        DifferentialFileTree current = this;
        String[] tp = path.getContent();
        for(int i = 0; i < tp.length -1; i++){
            current = current.getSubdir(tp[i]);
        }
        return current.getFile(tp[tp.length-1]);
    }

    private Map.Entry<List<String>, Patch<String>> getFile(String s) {
        return files.get(s);
    }
}
