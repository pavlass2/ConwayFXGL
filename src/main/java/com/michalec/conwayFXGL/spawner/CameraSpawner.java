package com.michalec.conwayFXGL.spawner;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.geometry.Point2D;

public class CameraSpawner implements EntityFactory {
    @Spawns("Camera")
    public static Entity spawnCamera() {
        return FXGL.entityBuilder(new SpawnData(new Point2D(0, 0))).buildAndAttach();
    }
}
