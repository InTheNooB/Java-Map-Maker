package app.creator;

import engine.AbstractGame;
import engine.GameContainer;
import engine.consts.FileConstants;
import static engine.consts.FileConstants.FOLDER_MAPS;
import engine.game.GameObject;
import static engine.game.GameObject.getGamePos;
import engine.game.GameObjectSprite;
import engine.game.animation.Animation;
import engine.game.physics.Physics;
import engine.ihm.game.Button;
import engine.ihm.game.ButtonEvent;
import engine.ihm.game.Popup;
import engine.ihm.menu.Menu;
import engine.io.SerialisationHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MapMaker extends AbstractGame implements FileConstants {

    // constants
    private final Font MENU_TITLE_FONT = new Font("TimesRoman", Font.BOLD, 100);
    private final Font MENU_BUTTON_FONT = new Font("TimesRoman", Font.BOLD, 20);
    private final Font TITLE_FONT = new Font("TimesRoman", Font.BOLD, 40);
    private final Font PPT_FONT = new Font("TimesRoman", Font.BOLD, 20);
    private final Font RESET_CAM_FONT = new Font("TimesRoman", Font.PLAIN, 12);
    private final int PPT_BG_W = 300;
    private final int PPT_BG_ALPHA = 150;
    private final int PPT_X_POS = PPT_BG_W - 10;
    public static final int GRID_CELL_W = 100;
    private final Color CENTER_MAP_POINT_COLOR = Color.ORANGE;
    public static final int RESIZE_SQUARE_SIZE = 10;

    // Shortcuts
    private final int[] SHORTCUT_SAVE = {KeyEvent.VK_CONTROL, KeyEvent.VK_S}; // CTRL + S
    private final int[] SHORTCUT_COPY = {KeyEvent.VK_CONTROL, KeyEvent.VK_C}; // CTRL + C
    private final int[] SHORTCUT_PASTE = {KeyEvent.VK_CONTROL, KeyEvent.VK_V}; // CTRL + V
    private final int[] SHORTCUT_RESET_CAM = {KeyEvent.VK_CONTROL, KeyEvent.VK_R}; // CTRL + R

    // Copy / Past
    private Block copiedBlock;

    // Backbone
    private HashMap<Integer, CopyOnWriteArrayList<Block>> blocks;
    private int currentLayer;

    private Block selectedBlock;
    private Menu mainMenu;
    private boolean clipGrid;

    // Properties Buttons
    private Button pptFixed;
    private Button pptTransparent;
    private Button pptPlatform;
    private Button pptSprite;
    private Button pptSpriteMod;

    private Button btnClipGrid;

    private Button btnLayer1;
    private Button btnLayer2;
    private Button btnLayer3;

    private Button btnSave;
    private Button btnBack;

    private Button btnResetCam;

    @Override
    public void setup(GameContainer gc) {
        gc.getSettings().setFullScreen(true);
        gc.getCam().setZoomLimits(Float.MIN_VALUE, 1);
        setupMenu(gc);
        blocks = new HashMap();
        blocks.put(1, new CopyOnWriteArrayList<>());
        blocks.put(2, new CopyOnWriteArrayList<>());
        currentLayer = 1;
        selectedBlock = null;

        try {
            // Look and feel of the fileChooser
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        }
    }

    private void setupProperties(GameContainer gc) {

        final int S_W = gc.getSettings().getWidth();
        final int S_H = gc.getSettings().getHeight();

        // Properties buttons
        // FIXED
        pptFixed = new Button(S_W - PPT_X_POS, 100, "Fixed");
        pptFixed.setFont(PPT_FONT);
        pptFixed.setBackgroundColor(Color.red);
        pptFixed.addListener(new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                if (selectedBlock == null) {
                    return;
                }
                selectedBlock.setFixed(!selectedBlock.isFixed());
                pptFixed.setBackgroundColor(selectedBlock.isFixed() ? Color.green : Color.red);
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addButton(pptFixed);

        //TRANSPARENT
        pptTransparent = new Button(S_W - PPT_X_POS, 150, "Transparent");
        pptTransparent.setFont(PPT_FONT);
        pptTransparent.setBackgroundColor(Color.red);
        pptTransparent.addListener(new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                if (selectedBlock == null) {
                    return;
                }

                selectedBlock.setTransparent(!selectedBlock.isTransparent());
                pptTransparent.setBackgroundColor(selectedBlock.isTransparent() ? Color.green : Color.red);
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addButton(pptTransparent);

        //PLATEFORM
        pptPlatform = new Button(S_W - PPT_X_POS, 200, "Platform");
        pptPlatform.setFont(PPT_FONT);
        pptPlatform.setBackgroundColor(Color.red);
        pptPlatform.addListener(new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                if (selectedBlock == null) {
                    return;
                }
                selectedBlock.setPlatform(!selectedBlock.isPlatform());
                pptPlatform.setBackgroundColor(selectedBlock.isPlatform() ? Color.green : Color.red);
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addButton(pptPlatform);

        //SPRITE
        pptSprite = new Button(S_W - PPT_X_POS, 250, "Sprite");
        pptSprite.setFont(PPT_FONT);
        pptSprite.setBackgroundColor(Color.magenta);
        pptSprite.addListener(new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                if (selectedBlock == null) {
                    return;
                }
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setCurrentDirectory(new File(FOLDER_IMAGES));
                int retour = fileChooser.showOpenDialog(null);
                if (retour == JFileChooser.APPROVE_OPTION) {
                    if (fileChooser.getSelectedFile().getName().endsWith(FILE_EXT_ANIMATION)) {
                        // animation
                        selectedBlock.setAnimation(gc, fileChooser.getSelectedFile().getName());
                        // set block size depending on the animation's sprites size
                        selectedBlock.setWidth(selectedBlock.getAnimation().getAnimations().get(0).getSpriteToDraw().getWidth(null));
                        selectedBlock.setHeight(selectedBlock.getAnimation().getAnimations().get(0).getSpriteToDraw().getHeight(null));
                    } else {
                        // image
                        selectedBlock.setSprite(new GameObjectSprite(gc, fileChooser.getSelectedFile().getName()));
                    }
                }
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addButton(pptSprite);

        //SPRITE_MOD
        pptSpriteMod = new Button(S_W - PPT_X_POS + 100, 250, "Stretch Sprite");
        pptSpriteMod.setFont(PPT_FONT);
        pptSpriteMod.setBackgroundColor(Color.cyan);
        pptSpriteMod.addListener(new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                if (selectedBlock == null) {
                    return;
                }
                pptSpriteMod.setContent(pptSpriteMod.getContent().equals("Stretch Sprite") ? "Repeat Sprite" : "Stretch Sprite");
                selectedBlock.setStretchSprite(!selectedBlock.isStretchSprite());
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addButton(pptSpriteMod);

        //CLIP GRID
        btnClipGrid = new Button(S_W - PPT_X_POS, 300, "Clip Grid");
        btnClipGrid.setFont(PPT_FONT);
        btnClipGrid.setBackgroundColor(Color.red);
        btnClipGrid.addListener(new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                clipGrid = !clipGrid;
                btnClipGrid.setBackgroundColor(clipGrid ? Color.green : Color.red);
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addButton(btnClipGrid);

        //LAYER 1
        btnLayer1 = new Button(S_W - PPT_X_POS, 650, "Layer 1");
        btnLayer1.setFont(PPT_FONT);
        btnLayer1.setBackgroundColor(Color.green);
        btnLayer1.addListener(new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                currentLayer = 1;
                if (selectedBlock != null) {
                    selectedBlock.setSelected(false);
                    selectedBlock = null;
                }
                btnLayer1.setBackgroundColor(Color.green);
                btnLayer2.setBackgroundColor(Color.red);
                btnLayer3.setBackgroundColor(Color.red);
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addButton(btnLayer1);

        //LAYER 2
        btnLayer2 = new Button(S_W - PPT_X_POS, 700, "Layer 2");
        btnLayer2.setFont(PPT_FONT);
        btnLayer2.setBackgroundColor(Color.red);
        btnLayer2.addListener(new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                currentLayer = 2;
                if (selectedBlock != null) {
                    selectedBlock.setSelected(false);
                    selectedBlock = null;
                }
                btnLayer1.setBackgroundColor(Color.red);
                btnLayer2.setBackgroundColor(Color.green);
                btnLayer3.setBackgroundColor(Color.red);
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addButton(btnLayer2);

        //LAYER 3
        btnLayer3 = new Button(S_W - PPT_X_POS, 750, "Layer 3");
        btnLayer3.setFont(PPT_FONT);
        btnLayer3.setBackgroundColor(Color.red);
        btnLayer3.addListener(new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                currentLayer = 3;
                if (selectedBlock != null) {
                    selectedBlock.setSelected(false);
                    selectedBlock = null;
                }
                btnLayer1.setBackgroundColor(Color.red);
                btnLayer2.setBackgroundColor(Color.red);
                btnLayer3.setBackgroundColor(Color.green);
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addButton(btnLayer3);

        // Save
        btnSave = new Button(S_W - PPT_X_POS, 850, "Save");
        btnSave.setFont(PPT_FONT);
        btnSave.setBackgroundColor(new Color(29, 126, 181));
        btnSave.addListener(new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                saveMap(gc);
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addButton(btnSave);

        // Back
        btnBack = new Button(S_W - PPT_X_POS, 900, "Back");
        btnBack.setFont(PPT_FONT);
        btnBack.setBackgroundColor(new Color(29, 126, 181));
        btnBack.addListener(new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                gc.getWindow().switchPanel("mainMenu");
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addButton(btnBack);

        // Reset cam
        btnResetCam = new Button(20, 30, "Reset Cam");
        btnResetCam.setFont(RESET_CAM_FONT);
        btnResetCam.setBackgroundColor(new Color(29, 126, 181));
        btnResetCam.addListener(new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                shortcutResetCam(gc);
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addButton(btnResetCam);

    }

    private void setupMenu(GameContainer gc) {
        updated = false;
        mainMenu = new Menu(gc);
        mainMenu.setTitle("Map Maker");
        mainMenu.setTitleFont(MENU_TITLE_FONT, Color.WHITE);
        mainMenu.setBackgroundImage("menu_bg.jpg");
        mainMenu.setButtonFont(MENU_BUTTON_FONT, Color.WHITE);
        mainMenu.setButtonSpacing(10);
        mainMenu.addButton("New Map", new Color(29, 126, 181), new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                newMap(gc);
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        mainMenu.addButton("Load Map", new Color(29, 126, 181), new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                loadMap(gc);
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        mainMenu.addButton("Exit", new Color(29, 126, 181), new ButtonEvent() {
            @Override
            public void onClick(GameContainer gc) {
                gc.endProgram();
            }

            @Override
            public void onHover(GameContainer gc) {
            }
        });
        gc.getWindow().addPanel("mainMenu", mainMenu);
        gc.getWindow().switchPanel("mainMenu");

    }

    private void newMap(GameContainer gc) {
        for (CopyOnWriteArrayList<Block> b : blocks.values()) {
            b.clear();
        }
        gc.getWindow().switchPanel("main");
        setupProperties(gc);
        updated = true;
    }

    private void saveMap(GameContainer gc) {
        String name = Popup.askData("Name of the map (no path / ext) :");
        if (name != null && !name.isEmpty()) {
            saveMap(gc, name);
        }
    }

    private void loadMap(GameContainer gc) {
        JFileChooser file = new JFileChooser();
        file.setFileSelectionMode(JFileChooser.FILES_ONLY);
        file.setCurrentDirectory(new File(FOLDER_MAPS));
        int retour = file.showOpenDialog(null);
        if (retour == JFileChooser.APPROVE_OPTION) {
            Map map = loadMap(gc, file.getSelectedFile().getAbsolutePath());
            if (map == null) {
                return;
            }
            blocks = map.getBlocks();
            if (blocks != null && !blocks.isEmpty()) {
                for (CopyOnWriteArrayList<Block> b : blocks.values()) {
                    for (Block block : b) {
                        block.setFixed(true);
                        if (block.getAnimation() != null) {
                            if (block.getAnimation().getAnimations() != null) {
                                for (Animation animation : block.getAnimation().getAnimations()) {
                                    animation.addSprites();
                                }
                            }
                        }
                    }
                }
                gc.getWindow().switchPanel("main");
                setupProperties(gc);
                updated = true;
            }
        }
    }

    private void resetButtons(GameContainer gc, Block b) {
        pptFixed.setBackgroundColor(b.isFixed() ? Color.green : Color.red);
        pptTransparent.setBackgroundColor(b.isTransparent() ? Color.green : Color.red);
        pptPlatform.setBackgroundColor(b.isPlatform() ? Color.green : Color.red);
        pptSpriteMod.setContent(b.isStretchSprite() ? "Stretch Sprite" : "Repeat Sprite");
    }

    @Override
    public void update(GameContainer gc, float dt) {

        // Save the map
        if (gc.getInput().isKeyDown(KeyEvent.VK_ENTER)) {
            saveMap(gc, FOLDER_MAPS + "map.mp");
        }

        // Add a block when spacebar is pressed
        if (gc.getInput().isKeyDown(KeyEvent.VK_SPACE)) {
            float x = gc.getInput().getMouseX();
            float y = gc.getInput().getMouseY();
            Block b = new Block((int) getGamePos(gc, x, y).x, (int) getGamePos(gc, x, y).y);
            blocks.get(currentLayer).add(b);
        }

        // Deletes a block
        if (gc.getInput().isKeyDown(KeyEvent.VK_DELETE)) {
            if (selectedBlock != null) {
                gc.removeGameObject(selectedBlock);
                blocks.get(currentLayer).remove(selectedBlock);
                selectedBlock = null;
            }
        }

        updateSelection(gc);
        if (selectedBlock != null) {
            updateMoveSelected(gc);
            updateResizeSelected(gc);
        }

        updateShortcuts(gc);
        for (CopyOnWriteArrayList<Block> block : blocks.values()) {
            for (Block b : block) {
                // updates the blocks
                b.update(gc, dt);
            }
        }

    }

    private void updateShortcuts(GameContainer gc) {
        // Save
        if (gc.getInput().isKey(SHORTCUT_SAVE[0]) && gc.getInput().isKeyDown(SHORTCUT_SAVE[1])) {
            shortcutSave(gc);
        }

        // Copy
        if (gc.getInput().isKey(SHORTCUT_COPY[0]) && gc.getInput().isKeyDown(SHORTCUT_COPY[1])) {
            shortcutCopy(gc);
        }

        // Paste
        if (gc.getInput().isKey(SHORTCUT_PASTE[0]) && gc.getInput().isKeyDown(SHORTCUT_PASTE[1])) {
            shortcutPaste(gc);
        }

        // Reset Cam
        if (gc.getInput().isKey(SHORTCUT_RESET_CAM[0]) && gc.getInput().isKeyDown(SHORTCUT_RESET_CAM[1])) {
            shortcutResetCam(gc);
        }

    }

    private void shortcutSave(GameContainer gc) {
        saveMap(gc);
    }

    private void shortcutCopy(GameContainer gc) {
        if (selectedBlock != null) {
            copiedBlock = selectedBlock;
        }
    }

    private void shortcutPaste(GameContainer gc) {
        if (selectedBlock != null) {
            selectedBlock.paste(copiedBlock);
        } else {
            float x = gc.getInput().getMouseX();
            float y = gc.getInput().getMouseY();
            Block b = new Block((int) getGamePos(gc, x, y).x, (int) getGamePos(gc, x, y).y);
            blocks.get(currentLayer).add(b);
            b.paste(copiedBlock);
        }
    }

    private void shortcutResetCam(GameContainer gc) {
        gc.getCam().resetCamPos(gc);
        gc.getCam().resetCamScale(gc);
    }

    private void updateResizeSelected(GameContainer gc) {
        if (selectedBlock == null || selectedBlock.isFixed()) {
            return;
        }

        if (gc.getInput().isKey(KeyEvent.VK_RIGHT)) {
            // Increase Width
            selectedBlock.increaseWidth();
        } else if (gc.getInput().isKey(KeyEvent.VK_LEFT)) {
            // Decrease Width
            selectedBlock.decreaseWidth();
        }
        if (gc.getInput().isKey(KeyEvent.VK_DOWN)) {
            // Increase Height
            selectedBlock.increaseHeight();
        } else if (gc.getInput().isKey(KeyEvent.VK_UP)) {
            // Decrease Height
            selectedBlock.decreaseHeight();
        }

        if (selectedBlock.isResizing()) {
            if (gc.getInput().isButtonUp(1)) {
                selectedBlock.setResizing(false);
                return;
            }
            selectedBlock.setWidth(getGamePos(gc, gc.getInput().getMouseX(), 0).x - selectedBlock.getX());
            selectedBlock.setHeight(getGamePos(gc, 0, gc.getInput().getMouseY()).y - selectedBlock.getY());
            if (selectedBlock.getWidth() <= 0) {
                selectedBlock.setWidth(1);
            }
            if (selectedBlock.getHeight() <= 0) {
                selectedBlock.setHeight(1);
            }
        } else if (gc.getInput().isButtonDown(1)) {
            int mX = gc.getInput().getMouseX();
            int mY = gc.getInput().getMouseY();
            if (Physics.collision(
                    selectedBlock.getX() + selectedBlock.getWidth(),
                    selectedBlock.getY() + selectedBlock.getHeight(),
                    RESIZE_SQUARE_SIZE,
                    RESIZE_SQUARE_SIZE,
                    getGamePos(gc, mX, mY).x,
                    getGamePos(gc, mX, mY).y,
                    1,
                    1)) {
                selectedBlock.setResizing(true);
            }

        }
    }

    private void updateMoveSelected(GameContainer gc) {
        if (selectedBlock == null || selectedBlock.isFixed()) {
            return;
        }

        // If he's moving
        if (selectedBlock.isMoving()) {
            if (clipGrid) {
                final float realW = (int) GameObject.getRealWidth(gc, GRID_CELL_W) > 0 ? GameObject.getRealWidth(gc, GRID_CELL_W) : 1;
                selectedBlock.moveClip(gc, gc.getInput().getMouseX(), gc.getInput().getMouseY(), realW);
            } else {
                selectedBlock.move(gc, gc.getInput().getMouseX(), gc.getInput().getMouseY());
            }
        }

        // If he's about to move ELSE he's stopping
        if (gc.getInput().isButtonDown(1)) {
            if (!selectedBlock.isMoving()) {
                int mX = gc.getInput().getMouseX();
                int mY = gc.getInput().getMouseY();
                if (Physics.collision(selectedBlock, getGamePos(gc, mX, mY).x, getGamePos(gc, mX, mY).y, 1, 1)) {
                    selectedBlock.startMoving(gc, mX, mY);
                }
            }
        } else if (gc.getInput().isButtonUp(1)) {
            selectedBlock.stopMoving(gc);
        }
    }

    private void updateSelection(GameContainer gc) {

        //Check if block is selected
        if (gc.getInput().isButtonDown(1)) {
            for (Block b : blocks.get(currentLayer)) {
                if (b.mouseCollision(gc, gc.getInput().getMouseX(), gc.getInput().getMouseY())) {
                    if (selectedBlock != null) {
                        //Unselect current
                        selectedBlock.setSelected(false);
                    }
                    //Select new one
                    selectedBlock = b;
                    b.setSelected(true);
                    resetButtons(gc, b);
                    break;
                }
            }
        }

        //Right click, then unselect
        if (gc.getInput().isButtonDown(3)) {
            if (selectedBlock != null) {
                //Unselect current
                selectedBlock.setSelected(false);
                selectedBlock = null;
            }

        }
    }

    private void saveMap(GameContainer gc, String filePath) {
        if ((filePath != null) && (!filePath.isEmpty())) {
            String mapName = filePath; // keeps track of the mapName of the map
            filePath = filePath.startsWith(FOLDER_MAPS) ? filePath : FOLDER_MAPS + filePath;
            filePath = filePath.endsWith(FILE_EXT_MAP) ? filePath : filePath + FILE_EXT_MAP;
            for (CopyOnWriteArrayList<Block> block : blocks.values()) {
                for (Block b : block) {
                    b.setSelected(false);
                }
            }
            Map map = new Map(blocks);
            map.setParameter("resolution", "1920x1080");
            map.setParameter("name", mapName);
            SerialisationHandler.serialiseObjet(gc, filePath, map);
        }
    }

    private Map loadMap(GameContainer gc, String filePath) {
        if ((filePath != null) && (!filePath.isEmpty())) {
            try {
                Map loadedMap = (Map) SerialisationHandler.deserialiseObjet(gc, filePath);
                return loadedMap;
            } catch (java.lang.ClassCastException e) {
                gc.getEventHistory().addEvent("Error loading the map : " + filePath);
            }
        }
        return null;
    }

    @Override
    public void render(GameContainer gc, Graphics2D g) {

        // Properties    
        final float S_W = gc.getSettings().getWidth();
        final float S_H = gc.getSettings().getHeight();

        // Render grid
        final float realW = (int) GameObject.getRealWidth(gc, GRID_CELL_W) > 0 ? GameObject.getRealWidth(gc, GRID_CELL_W) : 1;
        final float realH = (int) GameObject.getRealHeight(gc, GRID_CELL_W) > 0 ? GameObject.getRealHeight(gc, GRID_CELL_W) : 1;
        float offY = GameObject.getRealPos(gc, 0, 0).y;
        float offX = GameObject.getRealPos(gc, 0, 0).x;
        float flooredX = (float) (S_W / realW - Math.floor(S_W / realW));
        float flooredY = (float) (S_H / realH - Math.floor(S_H / realH));

        // Exemple :
        // => width         = 1500
        // => realW         = 61
        // => nbr           = 1500 / 61 = 24,5901
        // => nbr (int)     = 24
        // => floor (exced) = 0,5901
        // => per bloc      = 0,5901 * realW / nbr
        g.setColor(new Color(0, 0, 0, 50));

        for (float x = 0; x < S_W / realW - 1; x++) {
            float posX = realW;
            posX += (GameObject.getRealPos(gc, flooredX,0).x / Math.floor(S_W / realW));
            posX *= x;
            posX += offX;

            while (posX > S_W) {
                posX -= S_W;
            }
            while (posX < 0) {
                posX += S_W;
            }
            g.drawLine((int) posX, 0, (int) posX, (int) S_H);
        }

        for (float y = 0; y < S_H / realH - 1; y++) {
            float posY = realH;
            posY += (flooredY / Math.floor(S_H / realH));
            posY *= y;
            posY += offY;

            while (posY > S_H) {
                posY -= S_H;
            }
            while (posY < 0) {
                posY += S_H;
            }
            g.drawLine(0, (int) posY, (int) S_W, (int) posY);
        }

        g.setColor(Color.green);
        g.drawLine((int) offX, 0, (int) offX, (int) S_H);
        g.drawLine(0, (int) offY, (int) S_W, (int) offY);

        // Render blocks
        for (CopyOnWriteArrayList<Block> block : blocks.values()) {
            for (Block b : block) {
                b.render(gc, g);
            }
        }

        // Render center of the map
        g.setColor(CENTER_MAP_POINT_COLOR);
        g.fillOval(
                (int) GameObject.getRealPos(gc, S_W / 2 - 5, S_H / 2 - 5).x,
                (int) GameObject.getRealPos(gc, S_W / 2 - 5, S_H / 2 - 5).y,
                10,
                10);
//        g.fillOval(
//                (int) GameObject.getRealPos(gc, 0, 0).x,
//                (int) GameObject.getRealPos(gc, 0, 0).y,
//                20,
//                20);

        // Render a square to preview the screen
        g.setColor(Color.BLACK);
        g.drawRect(
                (int) GameObject.getRealPos(gc, 0, 0).x,
                (int) GameObject.getRealPos(gc, 0, 0).y,
                (int) GameObject.getRealWidth(gc, 1920),
                (int) GameObject.getRealWidth(gc, 1080));

        // Background
        g.setColor(new Color(0, 0, 0, PPT_BG_ALPHA));
        g.fillRect((int) S_W - PPT_BG_W, 0, PPT_BG_W, (int) S_H);

        // Cam pos (top left)
        g.setColor(Color.black);
        String txt = gc.getCam().getScale() + "";
        if (txt.length() > 4) {
            txt = txt.substring(0, 4);
        }
        g.drawString("Zoom lvl : " + txt, 20, 20);

        // Title
        g.setFont(TITLE_FONT);
        g.drawString("Properties", S_W - PPT_BG_W + PPT_BG_W / 5, 40);
    }

}
