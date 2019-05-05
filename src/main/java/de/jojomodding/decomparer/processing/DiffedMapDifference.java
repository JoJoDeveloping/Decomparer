package de.jojomodding.decomparer.processing;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.algorithm.myers.MyersDiff;
import com.github.difflib.patch.DeleteDelta;
import com.github.difflib.patch.Patch;

import java.util.*;

public class DiffedMapDifference<T,V> {
    private Map<T, List<V>> onlyLeft, onlyRight, equal;
    private Map<T, Map.Entry<List<V>, Patch<V>>> unequal, allpatched=new HashMap<>();

    public DiffedMapDifference(Map<T,List<V>> left, Map<T,List<V>> right) {
        onlyLeft = new HashMap<>(left);
        right.keySet().forEach(onlyLeft::remove);
        onlyRight = new HashMap<>(right);
        left.keySet().forEach(onlyRight::remove);
        equal = new HashMap<>(left);
        onlyLeft.keySet().forEach(equal::remove);
        unequal = new HashMap<>();
        for(Map.Entry<T, List<V>> me : equal.entrySet()){
            T k = me.getKey();
            List<V> v = me.getValue();
            if(!Objects.equals(v, right.get(k))) {
                try {
                    Map.Entry<List<V>, Patch<V>> a = Map.entry(v, DiffUtils.diff(v, right.get(k), new MyersDiff<>()));
                    unequal.put(k, a);
                    allpatched.put(k, a);
                } catch (DiffException e) {
                    e.printStackTrace();
                    unequal.put(k, Map.entry(v, null));
                    allpatched.put(k, Map.entry(v, null));
                }
            }
            else allpatched.put(k, Map.entry(v, new Patch<>()));
        };
        onlyLeft.forEach((k,v) -> {
            try {
                allpatched.put(k, Map.entry(v, DiffUtils.diff(v, List.of(), new MyersDiff<>())));
            } catch (DiffException e) {
                e.printStackTrace();
            }
        });
        onlyRight.forEach((k,v) -> {
            try {
                allpatched.put(k, Map.entry(List.of(), DiffUtils.diff(List.of(), v, new MyersDiff<>())));
            } catch (DiffException e) {
                e.printStackTrace();
            }
        });
        unequal.keySet().forEach(equal::remove);

    }

    public Set<T> getKeys() {
        Set<T> s = new HashSet<>();
        s.addAll(onlyLeft.keySet());
        s.addAll(onlyRight.keySet());
        s.addAll(equal.keySet());
        s.addAll(unequal.keySet());
        return s;
    }

    public Status describe(T key){
        if(unequal.containsKey(key))
            return Status.UNEQUAL;
        if(equal.containsKey(key))
            return Status.EQUAL;
        if(onlyLeft.containsKey(key))
            return Status.LEFT;
        if(onlyRight.containsKey(key))
            return Status.RIGHT;
        return null;
    }

    public Map.Entry<List<V>, Patch<V>> get(V s) {
        return allpatched.get(s);
    }

    public enum Status{
        LEFT, RIGHT, EQUAL, UNEQUAL;
    }
}
