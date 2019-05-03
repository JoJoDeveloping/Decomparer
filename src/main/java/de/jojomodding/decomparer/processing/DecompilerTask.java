package de.jojomodding.decomparer.processing;

import de.jojomodding.decomparer.config.SideConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DecompilerTask {

    private SideConfig conf;
    private File dir;
    private Process proc;

    public DecompilerTask(SideConfig conf, File outDir) throws IOException {
        this.conf = conf;
        this.dir = outDir;
        outDir.mkdirs();
        List<String> cmd = new ArrayList<>();
        cmd.add(conf.getJavaVirtualMachineExecutable());
        cmd.addAll(conf.getJavaArgs());
        cmd.add("-jar");
        cmd.add(conf.getDecompilerExecutable().getPath());
        cmd.addAll(conf.getExtraArgs());
        conf.getSources().stream().map(File::getPath).forEach(cmd::add);
        cmd.add(outDir.getPath());
        System.out.println("Running: "+ String.join(" ", cmd));
        proc = Runtime.getRuntime().exec(cmd.toArray(String[]::new));
        new Thread(() -> {
            BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream())), err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String prefix = conf.isLeft()?"LEFT:  ":"RIGHT: ";
            while(true){
                boolean fail=false;
                try {
                    System.out.println(prefix+out.readLine());
                } catch (IOException e) {
                    fail=true;
                }
                try {
                    System.err.println(prefix+err.readLine());
                } catch (IOException e) {
                    if(fail)
                        return;
                }
            }
        }).start();
    }

    public int waitFor(){
        while(proc.isAlive()){
            try {
                return proc.waitFor();
            } catch (InterruptedException e) {}
        }
        return proc.exitValue();
    }



}
