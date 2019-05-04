package de.jojomodding.decomparer.processing;

import de.jojomodding.decomparer.config.SideConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DecompilerTask {

    private SideConfig conf;
    private Process proc;

    public DecompilerTask(SideConfig conf, File outDir) throws IOException {
        this.conf = conf;
        outDir.mkdirs();
        List<String> cmd = new ArrayList<>();
        cmd.add(conf.getJavaVirtualMachineExecutable());
        cmd.addAll(conf.getJavaArgs());
        cmd.add("-jar");
        cmd.add(conf.getDecompilerExecutable().getPath());
        cmd.addAll(conf.getExtraArgs());
        cmd.add(conf.getSource().getPath());
        cmd.add(outDir.getPath());
        System.out.println("Running: "+ String.join(" ", cmd));
        proc = Runtime.getRuntime().exec(cmd.toArray(String[]::new));
        new Thread(redirecter(proc.getErrorStream(), System.err)).start();
        new Thread(redirecter(proc.getInputStream(), System.out)).start();
    }

    public Runnable redirecter(InputStream is, PrintStream os){
        return () -> {
            BufferedReader out = new BufferedReader(new InputStreamReader(is));
            String prefix = conf.isLeft()?"LEFT:  ":"RIGHT: ", s;
            try {
                while((s = out.readLine()) != null){
                    os.println(prefix+s);
                }
            }catch (IOException e){
                //
            }
        };
    }

    public int waitFor(){
        while(proc.isAlive()){
            try {
                return proc.waitFor();
            } catch (InterruptedException e) {}
        }
        return proc.exitValue();
//        return 0;
    }
}
