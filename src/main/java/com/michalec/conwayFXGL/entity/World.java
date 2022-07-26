package com.michalec.conwayFXGL.entity;

import com.michalec.conwayFXGL.spawner.FieldSpawner;
import com.michalec.conwayFXGL.valueObject.Preset;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class World {
    //private com.michalec.conway.Field[][] fields;
    private Field[][] fields;
    private int fieldSize;
    private Preset currentPreset;
    private List<List<Point2D>> history;


    public World(int worldSize, int fieldSize) {
        //super.set
        this.fieldSize = fieldSize;
        fields = new Field[worldSize][worldSize];
        history = new ArrayList<>();
        for (int y = 0; y < worldSize; y++) {
            for (int x = 0; x < worldSize; x++) {
                Field field = new Field(x, y, fieldSize);
                fields[x][y] = field;
            }
        }

        for (Field[] row : fields) {
            for (Field field : row) {
                field.setNeighbours(fields);
                field.setRectangle(FieldSpawner.spawnField(field));
            }
        }
    }

    public int getFieldSize() {
        return fieldSize;
    }
    public List<Point2D> getAliveFieldCoordinates() {
        ArrayList<Point2D> alive = new ArrayList<>();
        for (Field[] row : fields) {
            for (Field field : row) {
                if (field.isAlive()) {
                    alive.add(field.getCoordinates());
                }
            }
        }
        return alive;
    }

    public List<Point2D> getStartingAliveFields() {
        if (history == null || history.size() == 0) {
            // Game has not started yet.
            return getAliveFieldCoordinates();
        }
        return history.get(0);
    }

    protected Field[][] getFields() {
        return fields;
    };
    public void setCurrentPreset(Preset preset) {
        if (preset == null) {
            return;
        }
        currentPreset = preset;
    }

    public void preparePreset() {
        for (Field[] row : fields) {
            for (Field field : row) {
                if (this.currentPreset.getAliveFieldCoordinates().contains(field.getCoordinates())) {
                    field.setAlive(true);
                } else {
                    field.setAlive(false);
                }
            }
        }
    }

    public void update() {
        for (Field[] row : fields) {
            for (Field field : row) {
                int neighbourCount = 0;
                for (Field neighbour : field.getNeighbours()) {
                    if (neighbour.isAlive()) {
                        neighbourCount++;
                    }
                }

                if (field.isAlive() && (neighbourCount == 2 || neighbourCount == 3)) {
                    field.setAliveNextTime(field.isAlive());
                } else  if (neighbourCount == 3) {
                    field.setAliveNextTime(true);
                } else {
                    field.setAliveNextTime(false);
                }
            }
        }

        // add another history part
        history.add(new ArrayList<>());

        for (Field[] row : fields) {
            for (Field field : row) {
                // current state to history
                if (field.isAlive()) {
                    history.get(history.size() - 1).add(field.getCoordinates());
                }

                // field to next state
                field.setAlive(field.isAliveNextTime());
            }
        }
    }

    /**
     * Makes all fields default state (not alive).
     */
    public void reset() {
        for (Field[] row : fields) {
            for (Field field : row) {
                field.setAliveNextTime(false);
                field.setAlive(false);
            }
        }

        preparePreset();

        history = new ArrayList<>();
    }

    public void resetToStartState() {
        if (history == null || history.size() == 0) {
            return;
        }
        for (Field[] row : fields) {
            for (Field field : row) {
                field.setAliveNextTime(false);
                field.setAlive(history.get(0).contains(field.getCoordinates()));
            }
        }
        history = new ArrayList<>();
    }
}

