package com.michalec.conwayFXGL.data;

import com.michalec.conwayFXGL.valueObject.Preset;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class DynamicPreset extends Preset {
    List<Point2D> fieldsAlive = new ArrayList<>();
    public DynamicPreset() {

    }
    @Override
    public String getName() {
        return "Load preset from a file";
    }

    @Override
    public List<Point2D> getAliveFieldCoordinates() {
        return fieldsAlive;
    }
    public void addAliveFieldCoordinates(Point2D coordinates) {
        fieldsAlive.add(coordinates);
    }
}
