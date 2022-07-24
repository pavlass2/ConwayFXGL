package com.michalec.conwayFXGL.data;

import com.michalec.conwayFXGL.valueObject.Preset;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class GliderPreset extends Preset {
    List<Point2D> fields = new ArrayList<>();
    public GliderPreset() {
        fields.add(new Point2D(500, 500));
        fields.add(new Point2D(510, 510));
        fields.add(new Point2D(490, 520));
        fields.add(new Point2D(500, 520));
        fields.add(new Point2D(510, 520));
    }
    @Override
    public String getName() {
        return "Crawler";
    }

    @Override
    public List<Point2D> getAliveFieldCoordinates() {
        return fields;
    }
}
