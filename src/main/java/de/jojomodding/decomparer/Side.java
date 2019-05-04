package de.jojomodding.decomparer;

import de.jojomodding.decomparer.config.Configuration;
import de.jojomodding.decomparer.config.SideConfig;

import java.util.stream.Stream;

public enum Side {
    LEFT, RIGHT, BOTH;

    public Stream<SideConfig> allApplicable(Configuration conf){
        switch (this){
            case BOTH:
                return Stream.of(conf.getLeftSide(), conf.getRightSide());
            case LEFT:
                return Stream.of(conf.getLeftSide());
            case RIGHT:
                return Stream.of(conf.getRightSide());
        }
        throw new RuntimeException("Invalid enum value "+this);
    }

}
