package de.jojomodding.decomparer;

import de.jojomodding.decomparer.config.Configuration;
import de.jojomodding.decomparer.gui.DecomparerWindow;
import de.jojomodding.decomparer.processing.DifferentialFileTree;
import de.jojomodding.decomparer.processing.Processor;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Decomparer {

    public static void main(String[] args){
        try {
            Configuration conf = Configuration.readFromCommandLine(args);

            DifferentialFileTree pr = new Processor(conf).process();

            JFrame frame = new JFrame("Decomparer");
            frame.setPreferredSize(new Dimension(700, 700));
            frame.setMinimumSize(new Dimension(300, 200));
            frame.setContentPane(new DecomparerWindow(pr, conf).$$$getRootComponent$$$());
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
