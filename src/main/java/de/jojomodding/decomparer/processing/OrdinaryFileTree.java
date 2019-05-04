package de.jojomodding.decomparer.processing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class OrdinaryFileTree {
    private String name;
    private Map<String, OrdinaryFileTree> subdirs;
    private Map<String, List<String>> files;

    public OrdinaryFileTree(String name){
        this.name = name;
        subdirs = new HashMap<>();
        files = new HashMap<>();
    }

    public OrdinaryFileTree getSubdir(String name) {
        return subdirs.computeIfAbsent(name, OrdinaryFileTree::new);
    }

    public Set<String> getFilenames(String name){
        return files.keySet();
    }

    public List<String> getFile(String name) {
        return files.get(name);
    }

    public String getName(){
        return name;
    }

    public List<String> putFile(String name, List<String> content) {
        return files.put(name, content);
    }

    public Set<String> getSubdirs() {
        return subdirs.keySet();
    }

    public Map<String, List<String>> getFiles() {
        return new HashMap<>(files);
    }
}
