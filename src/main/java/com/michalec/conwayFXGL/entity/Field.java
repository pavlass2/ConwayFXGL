package com.michalec.conwayFXGL.entity;

import com.almasb.fxgl.entity.Entity;
import com.michalec.conwayFXGL.valueObject.Mode;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getWorldProperties;

public class Field {
    /**
     * x position in array
     */
    private int x;
    /**
     * y position in array
     */
    private int y;
    private Entity entity;

    /**
     * Position on screen
     */
    private Point2D coordinates;
    private Dimension2D size;
    private final List<Field> neighbours = new ArrayList<>();
    //private boolean[] neighboursAlive = new boolean[8];
    private boolean isAlive = false;
    private boolean isAliveNextTime = false;

    private Rectangle rectangle;

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
        rectangle.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent me) -> {
            if (me.getButton().equals(MouseButton.PRIMARY)) {
                Mode mode = getWorldProperties().getValue("MODE");

                if (mode.equals(Mode.SETUP)) {
                    if (isAlive()) {
                        setAlive(false);

                    } else {
                        setAlive(true);
                    }
                }
            }
        });
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        if (alive) {
            rectangle.setFill(Color.ORANGE);
        } else {
            rectangle.setFill(Color.WHITE);
        }
        isAlive = alive;
    }

    public boolean isAliveNextTime() {
        return isAliveNextTime;
    }

    public void setAliveNextTime(boolean aliveNextTime) {
        isAliveNextTime = aliveNextTime;
    }

    public List<Field> getNeighbours() {
        return neighbours;
    }

    public Point2D getCoordinates() {
        return coordinates;
    }

    public Dimension2D getSize() {
        return size;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Field(int x, int y, int size) {
        super();
        this.x = x;
        this.y = y;
        this.coordinates = new Point2D(x * size, y * size);
        this.size = new Dimension2D(size, size);
    }

    /**
     * Sets neighbours based on own coordinates.
     * Must be called before starting a game.
     * All fields must be fully created before calling this method.
     * Avoids accessing values beyond borders.
     * @param fields Collection of all fields which neighbouring fields will be grabbed from.
     */
    public void setNeighbours(Field[][] fields) {
        // Calculate borders
        boolean xLowerOk = x - 1 >= 0;
        boolean xHigherOk = fields.length > x + 1;
        boolean yLowerOk = y - 1 >= 0;
        boolean yHigherOk = fields[0].length > y + 1;

        // Access and add values within borders only.
        if (xLowerOk) {
            neighbours.add(fields[x - 1][y]);
            if (yLowerOk) {
                neighbours.add(fields[x][y - 1]);
                neighbours.add(fields[x - 1][y - 1]);
            }
            if (yHigherOk) {
                neighbours.add(fields[x - 1][y + 1]);
            }
        }
        if (xHigherOk) {
            neighbours.add(fields[x + 1][y]);
            if (yHigherOk) {
                neighbours.add(fields[x][y + 1]);
                neighbours.add(fields[x + 1][y + 1]);
            }
            if (yLowerOk) {
                neighbours.add(fields[x + 1][y - 1]);
            }
        }
    }
}
