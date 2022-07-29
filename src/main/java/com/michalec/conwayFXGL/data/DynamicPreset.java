package com.michalec.conwayFXGL.data;

import com.michalec.conwayFXGL.valueObject.Preset;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class DynamicPreset extends Preset {
    String name;
    List<Point2D> fieldsAlive = new ArrayList<>();
    public DynamicPreset(String name) {
        this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Point2D> getAliveFieldCoordinates() {
        return fieldsAlive;
    }
    public void addAliveFieldCoordinates(Point2D coordinates) {
        fieldsAlive.add(coordinates);
    }
    public void setAliveFieldCoordinates(List<Point2D> coordinatesList) {
        fieldsAlive = coordinatesList;
    }
}
