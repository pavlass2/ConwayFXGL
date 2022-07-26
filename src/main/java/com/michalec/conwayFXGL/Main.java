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
import com.michalec.conwayFXGL.data.StringStore;
import com.michalec.conwayFXGL.entity.DataMalformedException;
import com.michalec.conwayFXGL.entity.World;
import com.michalec.conwayFXGL.valueObject.CsvHeader;
import com.michalec.conwayFXGL.valueObject.Mode;
import com.michalec.conwayFXGL.valueObject.Preset;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import static com.michalec.conwayFXGL.valueObject.Mode.*;
import static com.michalec.conwayFXGL.valueObject.Mode.PAUSE;

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
    ImageView imgViewPlay;
    ImageView imgViewPause;
//    ImageView imgViewStepBackward;
//    ImageView imgViewStepForward;
    //endregion

    //region Components declaration
    Button btnPlayPause;
    Button btnStepBackward;
    Button btnStepForward;
    Button btnReset;
    FXGLButton btnSaveCustomPreset;
    FileChooser flCrSaveCustomPreset = new FileChooser();
    FileChooser flCrLoadCustomPreset = new FileChooser();
    Label lblSpeedValue;
    Slider sldrGameSpeed;

    Tooltip ttpStart;
    Tooltip ttpPause;
    Tooltip ttpUnpause;
    //endregion

    //region Events declaration
    EventHandler<ActionEvent> runModeHandler = event -> runMode();
    EventHandler<ActionEvent> setupModeHandler = event -> setupMode();
    EventHandler<ActionEvent> loadPresetFromFile = event -> {
        DynamicPreset dynamicPreset = new DynamicPreset();
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

        world.setCurrentPreset(dynamicPreset);
        world.reset();
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
            pause();
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
    EventHandler<ActionEvent> stepBackward = event -> {
        logger.info("BACKWARD");


    };
    EventHandler<ActionEvent> stepForward = event -> {
        world.update();
        if (getWorldProperties().getValue(MODE).equals(SETUP)) {
            pause();
        }

        btnReset.setDisable(false);
    };
    //endregion

    //region Private methods
    void runMode() {

        mainTimer = getGameTimer().runAtInterval(() -> world.update(), Duration.seconds(gameSpeed));
        getWorldProperties().setValue(MODE, RUN);

        btnReset.setDisable(false);
        btnReset.setOnAction(setupModeHandler);

        btnPlayPause.setGraphic(imgViewPause);
        btnPlayPause.setOnAction(e -> pause());
        btnPlayPause.setAccessibleHelp("Pause");
        btnPlayPause.setTooltip(ttpPause);

        btnStepForward.setDisable(true);
    }
    void setupMode() {
        if (mainTimer != null) {
            mainTimer.expire();
        }
        getWorldProperties().setValue(MODE, Mode.SETUP);
        btnReset.setDisable(true);

        btnPlayPause.setOnAction(runModeHandler);
        btnPlayPause.setGraphic(imgViewPlay);
        btnPlayPause.setTooltip(ttpStart);

        btnStepBackward.setDisable(true);
        btnStepForward.setDisable(false);

        world.resetToStartState();
    }

    void pause() { //TODO Udelej cudliky na pauzovani a krkovoani
        if (mainTimer != null && !mainTimer.isExpired()) {
            mainTimer.expire();
        }

        getWorldProperties().setValue(MODE, PAUSE);
        btnPlayPause.setGraphic(imgViewPlay);
        btnPlayPause.setOnAction(e -> unPause());
        btnPlayPause.setTooltip(ttpUnpause);
        btnStepBackward.setDisable(false);
        btnStepForward.setDisable(false);
    }

    void unPause() {
        mainTimer = getGameTimer().runAtInterval(() -> world.update(), Duration.seconds(gameSpeed));
        getWorldProperties().setValue(MODE, RUN);
        btnPlayPause.setGraphic(imgViewPause);
        btnPlayPause.setOnAction(e -> pause());
        btnPlayPause.setTooltip(ttpPause);
        btnStepBackward.setDisable(true);
        btnStepForward.setDisable(true);
    }
    void prepareGUI() {
        ImageView imgViewStepBackward = new ImageView(new Image("assets/ui/img/backward.png"));
        ImageView imgViewStepForward = new ImageView(new Image("assets/ui/img/forward.png"));
        ImageView imgViewReset = new ImageView(new Image("assets/ui/img/reset.png"));

        btnStepBackward = new Button();
        btnStepForward = new Button();
        btnReset = new Button();

        btnStepBackward.setGraphic(imgViewStepBackward);
        btnStepForward.setGraphic(imgViewStepForward);
        btnReset.setGraphic(imgViewReset);

        btnStepBackward.setTooltip(new Tooltip(STEP_BACKWARD));
        btnStepForward.setTooltip(new Tooltip(STEP_FORWARD));
        btnReset.setTooltip(new Tooltip(RESET));

        btnStepBackward.setOnAction(stepBackward);
        btnStepForward.setOnAction(stepForward);
        btnReset.setOnAction(setupModeHandler);


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
        sldrGameSpeed.setBlockIncrement(1);
        sldrGameSpeed.setMajorTickUnit(1);
        sldrGameSpeed.setMinorTickCount(0);
        sldrGameSpeed.setSnapToTicks(true);
        sldrGameSpeed.setOnMouseReleased(changeSpeed);

        VBox vBoxGameSpeed = new VBox(hboxLabelSpeed, sldrGameSpeed);
        vBoxGameSpeed.setAlignment(Pos.CENTER);


        btnPlayPause = new Button();
        imgViewPlay = new ImageView(new Image("assets/ui/img/play.png"));
        imgViewPause = new ImageView(new Image("assets/ui/img/pause.png"));

        ttpStart = new Tooltip(PLAY_CONWAYS_GAME_OF_LIFE);
        ttpPause = new Tooltip(StringStore.PAUSE);
        ttpUnpause = new Tooltip(UNPAUSE);


        MenuItem mniSaveAsPreset = new MenuItem(BTN_SAVE_PRESET);
        MenuItem mniLoadPreset = new MenuItem(LOAD_PRESET);

        mniSaveAsPreset.setOnAction(savePreset);
        mniLoadPreset.setOnAction(loadPresetFromFile);

        Menu menu = new Menu("Presets");
        menu.getItems().addAll(mniSaveAsPreset, mniLoadPreset, new SeparatorMenuItem());
        for (Preset preset : presetLoader.getPresets()) {
            MenuItem presetMenuItem = new MenuItem(preset.getName());
            presetMenuItem.setOnAction(e -> {
                world.setCurrentPreset(preset);
                world.reset();
            });
            menu.getItems().add(presetMenuItem);
        }
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(0, menu);

        HBox hboxMain = new HBox(btnReset, vBoxGameSpeed, btnStepBackward, btnPlayPause, btnStepForward, menuBar);
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