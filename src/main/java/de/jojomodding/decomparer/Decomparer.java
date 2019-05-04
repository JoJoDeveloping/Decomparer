package de.jojomodding.decomparer;

import de.jojomodding.decomparer.config.Configuration;
import de.jojomodding.decomparer.gui.DecomparerWindow;
import de.jojomodding.decomparer.processing.DifferentialFileTree;
import de.jojomodding.decomparer.processing.Processor;

import javax.swing.*;
import java.io.IOException;

public class Decomparer {

    public static void main(String[] args){
        try {
            Configuration conf = Configuration.readFromCommandLine(args);

            DifferentialFileTree pr = new Processor(conf).process();

            JFrame frame = new JFrame("Decomparer");
            frame.setContentPane(new DecomparerWindow(pr).panel);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }
    }

}
