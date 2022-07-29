package com.michalec.conwayFXGL;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.input.InputModifier;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.logging.Logger;
import com.almasb.fxgl.time.TimerAction;
import com.michalec.conwayFXGL.data.DynamicPreset;
import com.michalec.conwayFXGL.data.FilesystemDataProvider;
import com.michalec.conwayFXGL.data.PresetLoader;
import com.michalec.conwayFXGL.data.StringStore;
import com.michalec.conwayFXGL.entity.World;
import com.michalec.conwayFXGL.valueObject.Mode;
import com.michalec.conwayFXGL.valueObject.Preset;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getWorldProperties;
import static com.michalec.conwayFXGL.data.Constant.*;
import static com.michalec.conwayFXGL.data.StringStore.*;
import static com.michalec.conwayFXGL.valueObject.Mode.PAUSE;
import static com.michalec.conwayFXGL.valueObject.Mode.*;

public class Main extends GameApplication {
    //region Fields declaration
    private World world;
    private TimerAction mainTimer = null;
    private PresetLoader presetLoader = new PresetLoader();
    private Logger logger = Logger.get(Main.class);
    /**
     * Duration of time between game updates. The higher number, the slower game.
     * 1 = 1 Update every each second, 0.5 = 1 update every half a second.
     */
    private double gameSpeed = DEFAULT_GAME_SPEED;
    private ImageView imgViewPlay;
    private ImageView imgViewPause;
    //endregion

    //region Components declaration
    private Button btnPlayPause;
    private Button btnStepBackward;
    private Button btnStepForward;
    private Button btnReset;
    private Label lblSpeedValue;
    private Label lbl_moveNumber_val;
    private Slider sldrGameSpeed;
    private Menu mnu_preset;
    private Tooltip ttpStart;
    private Tooltip ttpPause;
    private Tooltip ttpUnpause;
    //endregion

    //region Events declaration
    private EventHandler<ActionEvent> runModeHandler = event -> runMode();
    private EventHandler<ActionEvent> setupModeHandler = event -> setupMode();
    private EventHandler<ActionEvent> loadPresetFromFile = event -> {
        DynamicPreset dynamicPreset = FilesystemDataProvider.loadFile();
        if (dynamicPreset == null) {
            return;
        }
        addMenuItemTo_mnu_preset(dynamicPreset);
        setupMode();
        world.setCurrentPreset(dynamicPreset);
        world.reset();
    };
    private EventHandler<ActionEvent> savePreset = event -> {
        String filename = FilesystemDataProvider.savePreset(world.getStartingAliveFields());
        if (filename != null) {
            DynamicPreset dynamicPreset = new DynamicPreset(filename);
            dynamicPreset.setAliveFieldCoordinates(world.getStartingAliveFields());
            addMenuItemTo_mnu_preset(dynamicPreset);
        }
    };
    private EventHandler<InputEvent> changeSpeed = event -> {
        gameSpeed = 1 - sldrGameSpeed.getValue() / 10;
        lblSpeedValue.setText(String.format("%.0f", sldrGameSpeed.getValue()));
        if (gameSpeed == 0) {
            gameSpeed = 0.01;
        } else if (gameSpeed == 1) {
            pause();
            return;
        }

        if (getWorldProperties().getValue(MODE).equals(RUN)) {
            mainTimer.expire();
            mainTimer = getGameTimer().runAtInterval(this::processNextMove, Duration.seconds(gameSpeed));
        }
    };
    private EventHandler<ActionEvent> stepBackward = event -> {
        world.backward();

        int moveNumber = world.getCurrentMoveNumber();
        lbl_moveNumber_val.setText(Integer.toString(moveNumber));
        btnStepBackward.setDisable(moveNumber < 1);
    };
    private EventHandler<ActionEvent> stepForward = event -> {
        processNextMove();
        if (getWorldProperties().getValue(MODE).equals(SETUP)) {
            pause();
        }

        btnReset.setDisable(false);
        btnStepBackward.setDisable(false);
    };
    //endregion

    //region Private methods
    private void processNextMove() {
        world.update();

        int moveNumber = world.getCurrentMoveNumber();
        lbl_moveNumber_val.setText(Integer.toString(moveNumber));
    }
    private void runMode() {

        mainTimer = getGameTimer().runAtInterval(this::processNextMove, Duration.seconds(gameSpeed));
        getWorldProperties().setValue(MODE, RUN);

        btnReset.setDisable(false);
        btnReset.setOnAction(setupModeHandler);

        btnPlayPause.setGraphic(imgViewPause);
        btnPlayPause.setOnAction(e -> pause());
        btnPlayPause.setAccessibleHelp("Pause");
        btnPlayPause.setTooltip(ttpPause);

        btnStepBackward.setDisable(true);
        btnStepForward.setDisable(true);
    }
    private void setupMode() {
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

        lbl_moveNumber_val.setText("0");

        world.resetToStartState();
    }

    private void pause() {
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

    private void unPause() {
        mainTimer = getGameTimer().runAtInterval(this::processNextMove, Duration.seconds(gameSpeed));
        getWorldProperties().setValue(MODE, RUN);
        btnPlayPause.setGraphic(imgViewPause);
        btnPlayPause.setOnAction(e -> pause());
        btnPlayPause.setTooltip(ttpPause);
        btnStepBackward.setDisable(true);
        btnStepForward.setDisable(true);
    }
    private void prepareGUI() {
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


        Label lbl_moveNumber_desc = new Label(MOVE_NUMBER);
        lbl_moveNumber_val = new Label();
        HBox hbox_MoveNumber = new HBox(lbl_moveNumber_desc, lbl_moveNumber_val);



        Label lblSpeedDescription = new Label("Game speed: ");
        lblSpeedValue = new Label("5");
        lblSpeedValue.setAlignment(Pos.CENTER_RIGHT);
        HBox hboxLabelSpeed = new HBox(lblSpeedDescription, lblSpeedValue);
        hboxLabelSpeed.setAlignment(Pos.CENTER);

        sldrGameSpeed = new Slider(1, 10, 5);
        sldrGameSpeed.setBlockIncrement(1);
        sldrGameSpeed.setMajorTickUnit(1);
        sldrGameSpeed.setMinorTickCount(0);
        sldrGameSpeed.setSnapToTicks(true);
        sldrGameSpeed.setOnMouseReleased(changeSpeed);
        sldrGameSpeed.setOnKeyReleased(changeSpeed);

        VBox vBoxGameSpeed = new VBox(hboxLabelSpeed, sldrGameSpeed);
        vBoxGameSpeed.setAlignment(Pos.CENTER);


        btnPlayPause = new Button();
        imgViewPlay = new ImageView(new Image("assets/ui/img/play.png"));
        imgViewPause = new ImageView(new Image("assets/ui/img/pause.png"));

        ttpStart = new Tooltip(PLAY_CONWAYS_GAME_OF_LIFE);
        ttpPause = new Tooltip(StringStore.PAUSE);
        ttpUnpause = new Tooltip(UNPAUSE);


        MenuItem mniSaveAsPreset = new MenuItem(SAVE_AS_PRESET);
        MenuItem mniLoadPreset = new MenuItem(LOAD_PRESET);

        mniSaveAsPreset.setOnAction(savePreset);
        mniLoadPreset.setOnAction(loadPresetFromFile);

        mnu_preset = new Menu("Presets");
        mnu_preset.getItems().addAll(mniSaveAsPreset, mniLoadPreset, new SeparatorMenuItem());
        for (Preset preset : presetLoader.getPresets()) {
            addMenuItemTo_mnu_preset(preset);
        }
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(0, mnu_preset);

        HBox hboxMain = new HBox(btnReset, vBoxGameSpeed, hbox_MoveNumber, btnStepBackward, btnPlayPause, btnStepForward, menuBar);
        hboxMain.setSpacing(10);
        hboxMain.setTranslateY(1005);
        hboxMain.setBackground(new Background(new BackgroundFill((Color.WHITE), CornerRadii.EMPTY, Insets.EMPTY)));
        FXGL.addUINode(hboxMain);
    }

    private void addMenuItemTo_mnu_preset(Preset preset) {
        MenuItem presetMenuItem = new MenuItem(FilenameUtils.removeExtension(preset.getName()));
        presetMenuItem.setOnAction(e -> {
            setupMode();
            world.setCurrentPreset(preset);
            world.reset();
        });
        mnu_preset.getItems().add(presetMenuItem);
    }
    //endregion

    //region Overridden FXGL protected methods
    /**
     * Initialize common game/window settings
     * @param gameSettings Setting for the game.
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
        ViewportManager viewportManager = new ViewportManager(getGameScene().getViewport());
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

        getInput().addAction(new UserAction("zoom_out") {
            @Override
            protected void onActionBegin() {
                viewportManager.zoomOut();
            }
        }, KeyCode.Q);
        getInput().addAction(new UserAction("zoom_in") {
            @Override
            protected void onActionBegin() {
                viewportManager.zoomIn();
            }
        }, KeyCode.E);
        getInput().addAction(new UserAction("zoom_out_mouse") {
            @Override
            protected void onAction() {
                viewportManager.zoomOut();
            }
        }, MouseButton.SECONDARY, InputModifier.CTRL);
        getInput().addAction(new UserAction("zoom_in_mouse") {
            @Override
            protected void onAction() {
                viewportManager.zoomIn();
            }
        }, MouseButton.PRIMARY, InputModifier.CTRL);
        getInput().addAction(new UserAction("zoom_out_mouse_wheel") {//TODO TEST
            @Override
            protected void onAction() {
                viewportManager.zoomOut();
            }
        }, MouseButton.BACK);
        getInput().addAction(new UserAction("zoom_in_mouse_wheel") {//TODO TEST
            @Override
            protected void onAction() {
                viewportManager.zoomIn();
            }
        }, MouseButton.FORWARD);

        getInput().addAction(new UserAction("up") {
            TimerAction movement = null;
            @Override
            protected void onActionBegin() {
                viewportManager.up();
                movement = getGameTimer().runAtInterval(() -> viewportManager.up(), Duration.seconds(CAMERA_MOVEMENT_DURATION));
            }
            @Override
            protected void onActionEnd() {
                if (movement != null) {
                    movement.expire();
                }
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("left") {
            TimerAction movement = null;
            @Override
            protected void onActionBegin() {
                viewportManager.left();
                movement = getGameTimer().runAtInterval(() -> viewportManager.left(), Duration.seconds(CAMERA_MOVEMENT_DURATION));
            }
            @Override
            protected void onActionEnd() {
                if (movement != null) {
                    movement.expire();
                }
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("down") {
            TimerAction movement = null;
            @Override
            protected void onActionBegin() {
                viewportManager.down();
                movement = getGameTimer().runAtInterval(() -> viewportManager.down(), Duration.seconds(CAMERA_MOVEMENT_DURATION));
            }
            @Override
            protected void onActionEnd() {
                if (movement != null) {
                    movement.expire();
                }
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("right") {
            TimerAction movement = null;
            @Override
            protected void onActionBegin() {
                viewportManager.right();
                movement = getGameTimer().runAtInterval(() -> viewportManager.right(), Duration.seconds(CAMERA_MOVEMENT_DURATION));
            }
            @Override
            protected void onActionEnd() {
                if (movement != null) {
                    movement.expire();
                }
            }
        }, KeyCode.D);
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