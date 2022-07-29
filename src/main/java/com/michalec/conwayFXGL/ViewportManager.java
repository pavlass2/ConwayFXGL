package com.michalec.conwayFXGL;

import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.Entity;
import com.michalec.conwayFXGL.spawner.CameraSpawner;

import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.michalec.conwayFXGL.data.Constant.CAMERA_MOVEMENT_SPEED;
import static com.michalec.conwayFXGL.data.Constant.DEFAULT_ZOOM;

public class ViewportManager {
    private Entity player = CameraSpawner.spawnCamera();
    private Viewport viewport;
    private Vec2 movementUp = new Vec2(0, 0 - CAMERA_MOVEMENT_SPEED);
    private Vec2 movementDown = new Vec2(0, CAMERA_MOVEMENT_SPEED);
    private Vec2 movementLeft = new Vec2(0 - CAMERA_MOVEMENT_SPEED, 0);
    private Vec2 movementRight = new Vec2(CAMERA_MOVEMENT_SPEED, 0);
    public ViewportManager(Viewport viewport) {
        this.viewport = viewport;
        viewport = getGameScene().getViewport();
        viewport.bindToEntity(player, 0, 0);
        viewport.setZoom(DEFAULT_ZOOM);
        viewport.setLazy(true);
    }
    public void zoomIn() {
        viewport.setZoom(viewport.getZoom() + 1);
    }
    public void zoomOut() {
        viewport.setZoom(viewport.getZoom() - 1);
    }
    public void up() {
        player.translate(movementUp);
    }
    public void down() {
        player.translate(movementDown);
    }
    public void left() {
        player.translate(movementLeft);
    }
    public void right() {
        player.translate(movementRight);
    }

}
