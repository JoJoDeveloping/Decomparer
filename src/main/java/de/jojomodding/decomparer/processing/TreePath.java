package de.jojomodding.decomparer.processing;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class TreePath implements Iterable<String>{

    private String[] data;

    public TreePath(){
        this.data = new String[0];
    }

    public TreePath(TreePath other, String sub){
        data = new String[other.data.length+1];
        System.arraycopy(other.data, 0, data, 0, other.data.length);
        data[data.length-1] = sub;
    }


    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return data.length < i;
            }

            @Override
            public String next() {
                return data[i++];
            }
        };
    }

    public TreePath resolve(String sub){
        return new TreePath(this, sub);
    }

    public String[] getContent(){
        return data;
    }
}
