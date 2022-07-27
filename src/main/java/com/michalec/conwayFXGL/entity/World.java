package com.michalec.conwayFXGL.entity;

import com.michalec.conwayFXGL.spawner.FieldSpawner;
import com.michalec.conwayFXGL.valueObject.Preset;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class World {
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

        Consumer<Field> setNeighbours = field -> {
            field.setNeighbours(fields);
            field.setRectangle(FieldSpawner.spawnField(field));
        };
        processFields(setNeighbours);
    }

    public void processFields(Consumer task) {
        for (Field[] row : fields) {
            for (Field field : row) {
                task.accept(field);
            }
        }
    }

    public int getFieldSize() {
        return fieldSize;
    }
    public List<Point2D> getAliveFieldCoordinates() {
        ArrayList<Point2D> alive = new ArrayList<>();
        Consumer<Field> addAlivefields = field -> {
            if (field.isAlive()) {
                alive.add(field.getCoordinates());
            }
        };
        processFields(addAlivefields);
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

    public int getCurrentMoveNumber() {
        if (history == null) {
            return 0;
        }
        return history.size();
    }

    public void preparePreset() {
        Consumer<Field> preparePreset = field -> {
            if (this.currentPreset.getAliveFieldCoordinates().contains(field.getCoordinates())) {
                field.setAlive(true);
            } else {
                field.setAlive(false);
            }
        };
        processFields(preparePreset);
    }

    public void update() {
        Consumer<Field> calculateNextMove = field -> {
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
        };
        Consumer<Field> processNextMove = field -> {
            // current state to history
            if (field.isAlive()) {
                history.get(history.size() - 1).add(field.getCoordinates());
            }

            // field to next state
            field.setAlive(field.isAliveNextTime());
        };

        processFields(calculateNextMove);
        // add another history part
        history.add(new ArrayList<>());
        processFields(processNextMove);

    }

    public void backward() {
        List<Point2D> pointInHistory = history.get(history.size() - 1);
        Consumer<Field> goBack = field -> {
            field.setAlive(pointInHistory.contains(field.getCoordinates()));
        };
        processFields(goBack);
        history.remove(history.get(history.size() - 1));
    }

    /**
     * Makes all fields default state (not alive).
     */
    public void reset() {
        Consumer<Field> reset = field -> field.setAlive(false);
        processFields(reset);
        preparePreset();

        history = new ArrayList<>();
    }

    public void resetToStartState() {
        if (history == null || history.size() == 0) {
            return;
        }
        Consumer<Field> reset = field -> field.setAlive(history.get(0).contains(field.getCoordinates()));
        processFields(reset);
        history = new ArrayList<>();
    }
}

