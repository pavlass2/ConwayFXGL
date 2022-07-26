package com.michalec.conwayFXGL;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.logging.Logger;
import com.almasb.fxgl.time.TimerAction;
import com.almasb.fxgl.ui.FXGLButton;
import com.michalec.conwayFXGL.data.DynamicPreset;
import com.michalec.conwayFXGL.data.PresetLoader;
import com.michalec.conwayFXGL.entity.DataMalformedException;
import com.michalec.conwayFXGL.entity.World;
import com.michalec.conwayFXGL.valueObject.CsvHeader;
import com.michalec.conwayFXGL.valueObject.Mode;
import com.michalec.conwayFXGL.valueObject.Preset;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getWorldProperties;
import static com.michalec.conwayFXGL.data.Constant.*;
import static com.michalec.conwayFXGL.data.StringStore.*;
import static com.michalec.conwayFXGL.valueObject.Mode.PAUSE;
import static com.michalec.conwayFXGL.valueObject.Mode.RUN;

public class Main extends GameApplication {
    //region Fields declaration
    World world;
    TimerAction mainTimer = null;
    PresetLoader presetLoader = new PresetLoader();;
    Logger logger = Logger.get(Main.class);
    /**
     * Duration of time between game updates. The higher number, the slower game.
     * 1 = 1 Update every each second, 0.5 = 1 update every half a second.
     */
    double gameSpeed = DEFAULT_GAME_SPEED;
    //endregion

    //region Components declaration
    FXGLButton btnChangeMode;
    FXGLButton btnSaveCustomPreset;
    ComboBox<Preset> cmbxPresetLoader;
    FileChooser flCrSaveCustomPreset = new FileChooser();
    FileChooser flCrLoadCustomPreset = new FileChooser();
    Label lblSpeedValue;
    Slider sldrGameSpeed;
    //endregion

    //region Events declaration
    EventHandler<ActionEvent> runModeHandler = event -> runMode();
    EventHandler<ActionEvent> setupModeHandler = event -> setupMode();
    EventHandler<ActionEvent> loadPreset = event -> {
        if (cmbxPresetLoader.getValue()  != null) {
            if (cmbxPresetLoader.getValue() instanceof DynamicPreset) {
                DynamicPreset dynamicPreset = (DynamicPreset)cmbxPresetLoader.getValue();
                File chosenFile = flCrLoadCustomPreset.showOpenDialog(FXGL.getPrimaryStage());
                if (chosenFile ==  null) {
                    logger.warning("No files were chosen.");
                    return;
                }
                    try {
                        Reader reader = new FileReader(chosenFile);
                        Iterable<CSVRecord> records = CSVFormat.Builder.create().setHeader(CsvHeader.x.name(), CsvHeader.y.name()).build().parse(reader);

                        for (CSVRecord record : records) {
                            if (!record.isConsistent()) {
                                throw new DataMalformedException(record.getRecordNumber(), record.toString(), chosenFile.getName());
                            }
                            String strX = record.get(CsvHeader.x.name());
                            String strY = record.get(CsvHeader.y.name());

                            if (strX.equals(CsvHeader.x.name()) || strY.equals(CsvHeader.y.name())) {
                                // Probably just header, ignore this record.
                                continue;
                            }

                            Double dblX = Double.valueOf(strX);
                            Double dblY = Double.valueOf(strY);

                            if (dblX < 0 || dblX > 990 || dblX % 10 != 0) {
                                throw new DataMalformedException(record.getRecordNumber(), dblX.toString(), chosenFile.getName());
                            }
                            if (dblY < 0 || dblY > 990 || dblY % 10 != 0) {
                                throw new DataMalformedException(record.getRecordNumber(), dblY.toString(), chosenFile.getName());
                            }

                            dynamicPreset.addAliveFieldCoordinates(new Point2D(dblX, dblY));
                        }
                    } catch (FileNotFoundException e) {
                        logger.warning("File " + chosenFile.getName() + " not found.");
                        e.printStackTrace();
                        return;
                    } catch (IOException e) {
                        logger.warning("An exception was thrown during reading file " + chosenFile.getName() + ".");
                        e.printStackTrace();
                        return;
                    } catch (DataMalformedException e) {
                        logger.warning("Some values in file " + chosenFile.getName() + " are malformed or invalid coordinates. Record number:" + e.getRecordNumber() + ", malformed data: " + e.getMalformedData() + ", file name: " + e.getFileName());
                        e.printStackTrace();
                        return;
                    } catch (NumberFormatException e) {
                        logger.warning("Some values in file " + chosenFile.getName() + " are not correctly formatted numbers.");
                        e.printStackTrace();
                        return;
                    }
                    // TODO Combobox zustane v pozici Load
                }

            world.setCurrentPreset(cmbxPresetLoader.getValue());
            world.reset();
        }
    };
    EventHandler<ActionEvent> savePreset = event -> {
        File chosenFile = flCrSaveCustomPreset.showSaveDialog(FXGL.getPrimaryStage());
        if (chosenFile == null) {
            return;
        }

        FileWriter out = null;
        try {
            out = new FileWriter(chosenFile);
            CSVPrinter printer = CSVFormat.Builder.create().setHeader(CsvHeader.x.name(), CsvHeader.y.name()).build().print(out);

            for (Point2D point2D : world.getStartingAliveFields()) {
                printer.printRecord((int)point2D.getX(), (int)point2D.getY());
            }
            printer.close(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };
    EventHandler<MouseEvent> changeSpeed = event -> {
        gameSpeed = 1 - sldrGameSpeed.getValue() / 10;
        lblSpeedValue.setText(String.format("%.0f", sldrGameSpeed.getValue()));
        if (gameSpeed == 0) {
            gameSpeed = 0.01;
        } else if (gameSpeed == 1) {
            pauseMode();
            return;
        }

        if (getWorldProperties().getValue(MODE).equals(PAUSE)) {
            unPause();
        }

        if (getWorldProperties().getValue(MODE).equals(RUN)) {
            mainTimer.expire();
            mainTimer = getGameTimer().runAtInterval(() -> world.update(), Duration.seconds(gameSpeed));
        }
    };
    //endregion

    //region Private methods
    void runMode() {

        mainTimer = getGameTimer().runAtInterval(() -> world.update(), Duration.seconds(gameSpeed));
        getWorldProperties().setValue(MODE, RUN);

        btnChangeMode.setText("Reset");
        btnChangeMode.setOnAction(setupModeHandler);

        cmbxPresetLoader.setDisable(true);
    }
    void setupMode() {
        if (mainTimer != null) {
            mainTimer.expire();
        }
        getWorldProperties().setValue(MODE, Mode.SETUP);
        btnChangeMode.setText("Begin the game of life");
        btnChangeMode.setOnAction(runModeHandler);

        cmbxPresetLoader.setDisable(false);

        if (cmbxPresetLoader.getValue() == null) {
            cmbxPresetLoader.getSelectionModel().selectFirst();
        }

        world.setCurrentPreset((Preset) cmbxPresetLoader.getValue());
        world.resetToStartState();
    }

    void pauseMode() { //TODO Udelej cudliky na pauzovani a krkovoani
        mainTimer.expire();
        getWorldProperties().setValue(MODE, PAUSE);
    }

    void unPause() {
        mainTimer = getGameTimer().runAtInterval(() -> world.update(), Duration.seconds(gameSpeed));
        getWorldProperties().setValue(MODE, RUN);
    }
    void prepareGUI() {
        btnChangeMode = new FXGLButton();
        HBox hbox = new HBox(btnChangeMode);
        hbox.setStyle(STYLE_BOTTOM_HBOX);

        btnSaveCustomPreset = new FXGLButton(BTN_SAVE_PRESET);
        btnSaveCustomPreset.setOnAction(savePreset);
        HBox hbox2 = new HBox(btnSaveCustomPreset);
        hbox2.setStyle(STYLE_BOTTOM_HBOX);

        cmbxPresetLoader = new ComboBox<>(FXCollections.observableArrayList(presetLoader.getPresets()));
        cmbxPresetLoader.setOnAction(loadPreset);

        flCrSaveCustomPreset.setTitle(FLCR_SAVE_PRESET);
        flCrSaveCustomPreset.setInitialFileName(DEFAULT_PRESET_FILE_NAME);

        flCrLoadCustomPreset.setTitle(LOAD_PRESET);
        flCrLoadCustomPreset.setInitialFileName(DEFAULT_PRESET_FILE_NAME);


        Label lblSpeedDescription = new Label("Game speed: ");
        lblSpeedValue = new Label("5");
        lblSpeedValue.setAlignment(Pos.CENTER_RIGHT);
        HBox hboxLabelSpeed = new HBox(lblSpeedDescription, lblSpeedValue);
        hboxLabelSpeed.setAlignment(Pos.CENTER);

        sldrGameSpeed = new Slider(0, 10, 5);
  //      sldrGameSpeed.setOrientation(Orientation.HORIZONTAL);
//        sldrGameSpeed.setShowTickMarks(true);
  //      sldrGameSpeed.setShowTickLabels(true);
       // sldrGameSpeed.setMajorTickUnit(10);
        //sldrGameSpeed.setBlockIncrement(1);
        //sldrGameSpeed.setSnapToTicks(true);
     //   sldrGameSpeed.setMinorTickCount(1);

        sldrGameSpeed.setBlockIncrement(1);
        sldrGameSpeed.setMajorTickUnit(1);
        sldrGameSpeed.setMinorTickCount(0);
        //sldrGameSpeed.setShowTickLabels(true);
        sldrGameSpeed.setSnapToTicks(true);


//        sldrGameSpeed.valueProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                gameSpeed = newValue.doubleValue();
//                lblSpeedValue.setText(Double.toString(gameSpeed));
//                if (getWorldProperties().getValue(MODE).equals(RUN)) {
//                    mainTimer.expire();
//                    mainTimer = getGameTimer().runAtInterval(() -> world.update(), Duration.seconds(gameSpeed));
//                }
//            }
//        });

        sldrGameSpeed.setOnMouseReleased(changeSpeed);


        VBox vBoxGameSpeed = new VBox(hboxLabelSpeed, sldrGameSpeed);
        vBoxGameSpeed.setAlignment(Pos.CENTER);

        MenuItem mniSaveAsPreset = new MenuItem(BTN_SAVE_PRESET);
        mniSaveAsPreset.setOnAction(savePreset);

        MenuItem mniLoadPreset = new MenuItem(LOAD_PRESET);
        mniLoadPreset.setOnAction(loadPreset);

        Menu menu = new Menu("Presets");
        menu.getItems().addAll(mniSaveAsPreset, new SeparatorMenuItem(), mniLoadPreset);
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(0, menu);

        HBox hboxMain = new HBox(hbox, hbox2, cmbxPresetLoader, vBoxGameSpeed, menuBar);
        hboxMain.setSpacing(10);
        hboxMain.setTranslateY(1005);
        FXGL.addUINode(hboxMain);
    }
    //endregion

    //region Overridden FXGL protected methods
    /**
     * Initialize common game/window settings
     * @param gameSettings
     */
    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle(NAME);
        gameSettings.setVersion(VERSION);
        gameSettings.setWidth(WIDTH);
        gameSettings.setHeight(HEIGHT);
    }
    @Override
    protected void initGame() {
        prepareGUI();

        world = new World(WORLD_SIZE, FIELD_SIZE);
        setupMode();

        getInput().addAction(new UserAction("start") {
            @Override
            protected void onActionBegin() {
                Mode mode = getWorldProperties().getValue(MODE);
                switch (mode) {
                    case SETUP -> runMode();
                    case RUN -> FXGL.getGameController().gotoGameMenu();
                }
            }
        }, KeyCode.SPACE);
    }
    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put(MODE, Mode.SETUP);
    }
    //endregion

    public static void main(String[] args) {
        launch(args);
    }
}