package de.jojomodding.decomparer.processing;

public class DifferentialFileTree {
    String name;
    SetDifference<DifferentialFileTree> subdirs;
    SetDifference<String> files;
}
