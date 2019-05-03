package de.jojomodding.decomparer.config;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.swing.text.html.Option;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Configuration {
    private SideConfig leftSide = new SideConfig(true), rightSide = new SideConfig(false);
    private File tempDir;
    private boolean parallel;

    public static Configuration readFromCommandLine(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        OptionSpec<File> decomiler = parser.acceptsAll(List.of("decompiler", "ff", "d"), "The decompiler to use, default fernflower.jar").withRequiredArg().ofType(File.class);
        OptionSpec<String> decargs = parser.acceptsAll(List.of("decompilerargs", "ffargs", "a"), "Arguments passed to the decompiler").withRequiredArg().ofType(String.class);
        OptionSpec<File> mainfile = parser.acceptsAll(List.of("file", "s", "f"), "The file to be modified").withRequiredArg().ofType(File.class);
        OptionSpec<File> tempdir = parser.acceptsAll(List.of("tempdir", "t"), "The temporary directory. Always affects both configurations").withRequiredArg().ofType(File.class);
        OptionSpec<String> java = parser.acceptsAll(List.of("jvm", "j"), "The jvm used to execute the decompiler, default java").withRequiredArg().ofType(String.class);
        OptionSpec<String> jvmargs = parser.acceptsAll(List.of("jvmarg", "x"), "Args passed to the jvm").withRequiredArg().ofType(String.class);
        OptionSpec helper = parser.acceptsAll(List.of("help", "h", "?"), "Shows help").forHelp();
        OptionSpec both = parser.acceptsAll(List.of("both", "b"), "Following modifiers affect both decompiler configurations. Default");
        OptionSpec left = parser.acceptsAll(List.of("left", "l"), "Following modifiers affect only the source/left configuration");
        OptionSpec right = parser.acceptsAll(List.of("right", "r"), "Following modifiers affect only the target/right configuration");
        OptionSpec parallel = parser.acceptsAll(List.of("parallel", "p"), "Run both decompilers in parallel");
        List<String> latestSubargs = new ArrayList<>(args.length);
        Side side = Side.BOTH, nextside=null;
        Configuration conf = new Configuration();
        OptionSet os = parser.parse(args);
        if(os.has(helper)){
            parser.printHelpOn(System.out);
            System.exit(0);
        }
        int findex=0, dcindex=0, argindex=0, jfindex=0, jaindex=0;
        Side currentSide = Side.BOTH;
        for(OptionSpec s : os.specs()){
            if(s == left)
                currentSide = Side.LEFT;
            else if(s == right)
                currentSide = Side.RIGHT;
            else if(s == both)
                currentSide = Side.BOTH;
            else if(s == decargs){
                String arg = os.valuesOf(decargs).get(argindex++);
                currentSide.allApplicable(conf).forEach(sc -> sc.getRawExtraArgs().add(arg));
            }else if(s == decomiler){
                File decompiler = os.valuesOf(decomiler).get(dcindex++);
                currentSide.allApplicable(conf).forEach(sc -> sc.setDecompilerExecutable(decompiler));
            }else if(s == mainfile){
                File f = os.valuesOf(mainfile).get(findex++);
                currentSide.allApplicable(conf).forEach(sc -> sc.getRawSources().add(f));
            }else if(s == java){
                String jx = os.valuesOf(java).get(jfindex++);
                currentSide.allApplicable(conf).forEach(sc -> sc.setJavaVirtualMachineExecutable(jx));
            }else if(s == jvmargs){
                String jx = os.valuesOf(jvmargs).get(jaindex++);
                currentSide.allApplicable(conf).forEach(sc -> sc.getRawJavaArgs().add(jx));
            }else if(s == tempdir){
                conf.tempDir = os.valueOf(tempdir);
            }else if(s == parallel){
                conf.parallel = true;
            }
        }
        if(conf.tempDir == null){
            conf.tempDir = Files.createTempDirectory("decomparer").toFile();
        }else{
            conf.tempDir = Files.createDirectory(conf.tempDir.toPath()).toFile();
        }
        System.out.println("Tempdir: "+conf.tempDir);
        Side.BOTH.allApplicable(conf).forEach(c -> {
            System.out.println("    FF: " + c.getDecompilerExecutable());
            System.out.println("    FF args: "+ String.join(",", c.getExtraArgs()));
            System.out.println("    sources: "+ c.getSources().stream().map(Object::toString).collect(Collectors.joining(",")));
            System.out.println("    Java   : "+ c.getJavaVirtualMachineExecutable());
            System.out.println("    JVM Arg: "+ String.join(",", c.getJavaArgs()));
        });
        if(Side.BOTH.allApplicable(conf).anyMatch(c -> c.getSources().isEmpty())){
            throw new IOException("You are missing the sources");
        }
        return conf;
    }

    public SideConfig getLeftSide() {
        return leftSide;
    }

    public SideConfig getRightSide() {
        return rightSide;
    }

    public File getTempDir() {
        return tempDir;
    }

    public boolean isParallel() {
        return parallel;
    }


    private enum Side {
        LEFT, RIGHT, BOTH;

        public Stream<SideConfig> allApplicable(Configuration conf){
            switch (this){
                case BOTH:
                    return Stream.of(conf.leftSide, conf.rightSide);
                case LEFT:
                    return Stream.of(conf.leftSide);
                case RIGHT:
                    return Stream.of(conf.rightSide);
            }
            throw new RuntimeException("Invalid enum value "+this);
        }

    }

}
