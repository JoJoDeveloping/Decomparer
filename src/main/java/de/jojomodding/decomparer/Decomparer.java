package de.jojomodding.decomparer;

import com.sun.tools.javac.util.List;
import joptsimple.OptionParser;

import java.io.File;

public class Decomparer {

    public static void main(String args){
        OptionParser parser = new OptionParser("h*");
        parser.acceptsAll(List.of("decompiler", "ff", "d", "f"), "The decompiler to use").withRequiredArg().ofType(File.class).required();
        parser.acceptsAll(List.of("decompilerargs", "ffargs", "a"), "Arguments passed to the decompiler").withRequiredArg().ofType(String.class);
        parser.acceptsAll(List.of("left", "file1", "l"), "The file on the left, also considered the original file").withRequiredArg().ofType(File.class).required();
        parser.acceptsAll(List.of("right", "file2", "r"), "The file on the right, also considered the changed file").withRequiredArg().ofType(File.class).required();
        parser.acceptsAll(List.of("tempdir", "t"), "The temporary directory").withRequiredArg().ofType(File.class).defaultsTo(new File(System.getProperty("java.io.tmpdir")+File.separatorChar+"decomparer"))
    }

}
