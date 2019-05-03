package de.jojomodding.decomparer.processing;

import de.jojomodding.decomparer.config.Configuration;

import java.io.File;
import java.io.IOException;

public class Processor {

    private Configuration config;

    public Processor(Configuration c){
        this.config = c;
    }

    public ProcessResult process() throws IOException {
        DecompilerTask left = new DecompilerTask(config.getLeftSide(), new File(config.getTempDir(), "leftResult"));
        if(left.waitFor() != 0)
            throw new IOException("Left decompiler returned non-null!");
        DecompilerTask right = new DecompilerTask(config.getRightSide(), new File(config.getTempDir(), "right"));
        if(right.waitFor() != 0)
            throw new IOException("Right decompiler returned non-null!");
        return null;
    }

}
