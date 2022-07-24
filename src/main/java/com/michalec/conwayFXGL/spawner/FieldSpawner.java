package com.michalec.conwayFXGL.spawner;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.logging.Logger;
import com.michalec.conwayFXGL.entity.Field;
import com.michalec.conwayFXGL.valueObject.Mode;
import javafx.geometry.Dimension2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getWorldProperties;

public class FieldSpawner implements EntityFactory {

    @Spawns("field")
    public static Rectangle spawnField(Field field) {
        Rectangle rectangle = new Rectangle();
        rectangle.setHeight(field.getSize().getHeight());
        rectangle.setWidth(field.getSize().getWidth());
        rectangle.setStroke(Color.GRAY);
        rectangle.setFill(Color.WHITE);

        FXGL.entityBuilder(new SpawnData(field.getCoordinates())).view(rectangle).buildAndAttach();
        return rectangle;
    }
}
