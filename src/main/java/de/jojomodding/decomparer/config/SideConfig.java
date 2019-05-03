package de.jojomodding.decomparer.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SideConfig {

    private File decompilerExec = new File("fernflower.jar");
    private List<String> extraArgs = new ArrayList<>();
    private List<File> sources = new ArrayList<>();
    private String jvm = "java";
    private List<String> jvmArgs = new ArrayList<>();
    private boolean isLeft;

    public SideConfig(boolean left){
        this.isLeft = left;
    }

    public boolean isLeft(){
        return isLeft;
    }

    public File getDecompilerExecutable(){
        return decompilerExec;
    }

    protected void setDecompilerExecutable(File f){
        this.decompilerExec = f;
    }

    public String getJavaVirtualMachineExecutable(){
        return jvm;
    }

    protected void setJavaVirtualMachineExecutable(String s){
        jvm = s;
    }

    public List<String> getExtraArgs(){
        return new ArrayList<>(extraArgs);
    }

    protected List<String> getRawExtraArgs(){
        return extraArgs;
    }

    protected void setExtraArgs(List<String> s){
        this.extraArgs = s;
    }

    public List<File> getSources(){
        return new ArrayList<>(sources);
    }

    protected List<File> getRawSources(){
        return sources;
    }

    protected void setSources(List<File> s){
        this.sources = s;
    }

    public List<String> getJavaArgs(){
        return new ArrayList<>(jvmArgs);
    }

    protected List<String> getRawJavaArgs(){
        return jvmArgs;
    }

    protected void setJavaArgs(List<String> s){
        this.jvmArgs = s;
    }

}
