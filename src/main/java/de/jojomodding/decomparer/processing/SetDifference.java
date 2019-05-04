package de.jojomodding.decomparer.processing;

import java.util.HashSet;
import java.util.Set;

public class SetDifference<T> {

    private Set<T> both;
    private Set<T> leftOnly;
    private Set<T> rightOnly;

    public SetDifference(Set<T> left, Set<T> right){
        leftOnly = new HashSet<>(left);
        leftOnly.removeAll(right);
        rightOnly = new HashSet<>(right);
        rightOnly.removeAll(left);
        both = new HashSet<>(left);
        both.removeAll(leftOnly);
    }

    public Set<T> getCommon(){
        return both;
    }

    public Set<T> getLeftOnly(){
        return leftOnly;
    }

    public Set<T> getRightOnly(){
        return rightOnly;
    }

}
