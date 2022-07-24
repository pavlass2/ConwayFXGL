package com.michalec.conwayFXGL.data;

import com.michalec.conwayFXGL.valueObject.Preset;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class EmptyPreset extends Preset {
    ArrayList<Point2D> fields = new ArrayList<>();

    @Override
    public String getName() {
        return "No preset";
    }

    @Override
    public List<Point2D> getAliveFieldCoordinates() {
        return fields;
    }
}
