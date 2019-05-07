package de.jojomodding.decomparer.config;

import de.jojomodding.decomparer.Side;
import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private SideConfig leftSide = new SideConfig(true), rightSide = new SideConfig(false);
    private File tempDir;
    private String font="Monospaced";
    private boolean parallel, skip;

    public static Configuration readFromCommandLine(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        OptionSpec<File> decomiler = parser.acceptsAll(List.of("decompiler", "ff", "d"), "SIDED - The decompiler to use, default fernflower.jar").withRequiredArg().ofType(File.class);
        OptionSpec<String> decargs = parser.acceptsAll(List.of("decompilerargs", "ffargs", "a"), "SIDED - Arguments passed to the decompiler").withRequiredArg().ofType(String.class);
        OptionSpec<File> mainfile = parser.acceptsAll(List.of("file", "s", "f"), "SIDED - The file to be modified").withRequiredArg().ofType(File.class);
        OptionSpec<File> tempdir = parser.acceptsAll(List.of("tempdir", "t"), "The temporary directory.").withRequiredArg().ofType(File.class);
        OptionSpec<String> java = parser.acceptsAll(List.of("jvm", "j"), "SIDED - The jvm used to execute the decompiler, default java").withRequiredArg().ofType(String.class);
        OptionSpec<String> jvmargs = parser.acceptsAll(List.of("jvmarg", "x"), "SIDED - Args passed to the jvm").withRequiredArg().ofType(String.class);
        OptionSpec helper = parser.acceptsAll(List.of("help", "h", "?"), "Shows help").forHelp();
        OptionSpec both = parser.acceptsAll(List.of("both", "b"), "Following sided modifiers affect both sides");
        OptionSpec left = parser.acceptsAll(List.of("left", "l"), "Following sided modifiers affect only the left/source side");
        OptionSpec right = parser.acceptsAll(List.of("right", "r"), "Following sided modifiers affect only the right/target side");
        OptionSpec parallel = parser.acceptsAll(List.of("parallel", "p"), "Run both decompilers in parallel");
        OptionSpec skip = parser.acceptsAll(List.of("skipdecompile"), "Skip decompiling, use result files already present");
        OptionSpec<String> fontname = parser.accepts("font", "The editor font").withRequiredArg().defaultsTo("Monospaced");
        List<String> latestSubargs = new ArrayList<>(args.length);
        Side side = Side.BOTH, nextside=null;
        Configuration conf = new Configuration();
        OptionSet os = parser.parse(args);
        if(os.has(helper)){
            System.out.println("This program decompiles two jar files and allows you to compare the results.");
            System.out.println("Example: java -jar decomparer.jar -d fernflower.jar -lf leftfile.jar -rf rightfile.jar -p");
            System.out.println();
            parser.formatHelpWith(new BuiltinHelpFormatter(140, 2));
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
                currentSide.allApplicable(conf).forEach(sc -> sc.setSource(f));
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
            }else if(s == skip){
                conf.skip = true;
            }else if(s == fontname){
                conf.font = os.valueOf(fontname);
            }
        }
        if(conf.tempDir == null){
            conf.tempDir = Files.createTempDirectory("decomparer").toFile();
        }else{
            conf.tempDir.mkdirs();
        }
//        System.out.println("Tempdir: "+conf.tempDir);
//        Side.BOTH.allApplicable(conf).forEach(c -> {
//            System.out.println("    FF: " + c.getDecompilerExecutable());
//            System.out.println("    FF args: "+ String.join(",", c.getExtraArgs()));
//            System.out.println("    sources: "+ c.getSource());
//            System.out.println("    Java   : "+ c.getJavaVirtualMachineExecutable());
//            System.out.println("    JVM Arg: "+ String.join(",", c.getJavaArgs()));
//        });
        if(Side.BOTH.allApplicable(conf).anyMatch(c -> c.getSource() == null)){
            throw new IOException("You are missing the source!");
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

    public String getFontName(){
        return font;
    }

    public boolean skipsDecompilation() {
        return skip;
    }
}
