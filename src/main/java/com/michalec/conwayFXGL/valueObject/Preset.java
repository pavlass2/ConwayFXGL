package com.michalec.conwayFXGL.valueObject;

import javafx.geometry.Point2D;

import java.util.List;

public abstract class Preset {
    public abstract String getName();
    public abstract List<Point2D> getAliveFieldCoordinates();

    @Override
    public String toString() {
        return getName();
    }
}
