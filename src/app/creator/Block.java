package app.creator;

import static app.creator.MapMaker.RESIZE_SQUARE_SIZE;
import engine.GameContainer;
import engine.game.GameObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import app.settings.Constants;
import static engine.game.GameObject.getRealPos;
import static engine.game.GameObject.getRealWidth;
import engine.game.Orientation;
import engine.game.animation.AnimationSet;
import java.awt.BasicStroke;
import java.awt.Graphics;

public class Block extends GameObject implements Constants, Serializable {

    transient private boolean selected;

    transient private int movingStartX;
    transient private int movingStartY;
    transient private boolean resizing;
    transient private boolean moving;
    transient private boolean fixed;

    // In game attributs
    private boolean transparent;
    private boolean platform;
    private boolean stretchSprite;

    public Block(int x, int y) {
        createDefaultShape(x, y);
        selected = false;
        selectable = false;
        rendered = false;
        updated = false;
    }

    /**
     * Creates a default square shape at the x and y position
     *
     * @param x The X origin
     * @param y The Y origin
     */
    private void createDefaultShape(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = DEFAULT_BLOCK_WIDTH;
        this.height = DEFAULT_BLOCK_HEIGHT;
    }

    /**
     * Check if the mouse (x,y) interesects the shape.
     *
     * @param gc
     * @param x The x (mouse)
     * @param y The y (mouse)
     * @return True if there is a collision.
     */
    public boolean mouseCollision(GameContainer gc, int x, int y) {
        return getOnScreenShape(gc).contains(x, y);
    }

    public void stopMoving(GameContainer gc) {
        moving = false;
    }

    public void startMoving(GameContainer gc, int x, int y) {
        movingStartX = (int) getGamePos(gc, x, y).x;
        movingStartY = (int) getGamePos(gc, x, y).y;
        moving = true;
    }

    /**
     * Moves the shape at a X and Y coords if the mouse hovers it.
     *
     * @param gc
     * @param x
     * @param y
     */
    public void move(GameContainer gc, int x, int y) {
        this.x = (int) (this.x + (getGamePos(gc, x, y).x - movingStartX));
        this.y = (int) (this.y + (getGamePos(gc, x, y).y - movingStartY));

        movingStartX = (int) getGamePos(gc, x, y).x;
        movingStartY = (int) getGamePos(gc, x, y).y;
    }

    public void moveClip(GameContainer gc, int x, int y, float cellW) {

        // Properties    
        final float S_W = gc.getSettings().getWidth();
        final float S_H = gc.getSettings().getHeight();

        // Find cell in wich the cursor currently is
        float offX = GameObject.getRealPos(gc, 0, 0).x;
        float offY = GameObject.getRealPos(gc, 0, 0).y;
        float flooredX = (float) Math.floor(S_W / cellW);
        float flooredY = (float) Math.floor(S_H / cellW);
        float decimalsX = (float) (S_W / cellW - flooredX);
        float decimalsY = (float) (S_H / cellW - flooredY);
//        int cellX = (int) ((x - offX) / (cellW + (decimalsX  / flooredX)) + (decimalsX * (x/S_W)));
        int cellX = (int) ((x - offX) / (cellW + (decimalsX / flooredX)));
        int cellY = (int) ((y - offY) / (cellW + (decimalsY / flooredY)));

//        cellX = cellX <= 0 ? cellX - 1 : cellX;
//        cellY = cellY <= 0 ? cellY - 1 : cellY;
//        System.out.println("X : " + x);
//        System.out.println("offX : " + offX);
//        System.out.println("flooredX : " + flooredX);
//        System.out.println("cellX : " + cellX);
    
        float posX = cellW;
        posX += (decimalsX / Math.floor(S_W / cellW)); // add exced divided by the nbr of blocks
        posX *= cellX;
        posX += offX;
        float posY = cellW;
        posY += (decimalsY / Math.floor(S_H / cellW));
        posY *= cellY;
        posY += offY;

        this.x = (int) getGamePos(gc, posX, posY).x;
        this.y = (int) getGamePos(gc, posX, posY).y;

    }

    public void increaseWidth() {
        width += SIZE_INCREASE;
    }

    public void decreaseWidth() {
        if (width > SIZE_INCREASE) {
            width -= SIZE_INCREASE;
        }
    }

    public void increaseHeight() {
        height += SIZE_INCREASE;
    }

    public void decreaseHeight() {
        if (height > SIZE_INCREASE) {
            height -= SIZE_INCREASE;
        }
    }

    public Point calculateCentroid() {
        return new Point((int) (x + width / 2), (int) (y + height / 2));
    }

    public Rectangle getOnScreenShape(GameContainer gc) {
        int x_ = (int) getRealPos(gc, x, 0).x;
        int y_ = (int) getRealPos(gc, 0, y).y;
        int w_ = (int) getRealWidth(gc, width);
        int h_ = (int) getRealWidth(gc, height);

        return new Rectangle(x_, y_, w_, h_);
    }

    @Override
    public void setup(GameContainer gc) {
    }

    @Override
    public void setupAnimation(GameContainer gc) {
    }

    @Override
    public void update(GameContainer gc, float dt) {
        updateAnimation(gc);
    }

    public void setAnimation(GameContainer gc, String name) {
        animation = new AnimationSet();
        sprite = null;
        idleAnimation = animation.importAnimation(name);
        animation.setCurrentCategorie(idleAnimation);
    }

    @Override
    public void render(GameContainer gc, Graphics2D g) {

        // Shape
        if (sprite != null) {
//            drawSprite(gc, g);
            if (stretchSprite) {
                // Draw stretched sprite
                g.drawImage(sprite.getImage(), (int) getRealPos(gc).x, (int) getRealPos(gc).y, (int) (width * gc.getCam().getScale()), (int) (height * gc.getCam().getScale()), null);
            } else {
                // Repeat sprite pattern
                int maxX = (int) (width / sprite.getW()) + 1;
                int maxY = (int) (height / sprite.getH()) + 1;
                int defaultW = (int) sprite.getW();
                int defaultH = (int) sprite.getH();
                for (int i = 0; i < maxX; i++) {
                    for (int j = 0; j < maxY; j++) {
                        int w = defaultW;
                        int h = defaultH;

                        if (defaultW * (i + 1) > width) {
                            w = (int) (width - defaultW * i);
                        }

                        if (defaultH * (j + 1) > height) {
                            h = (int) (height - defaultH * j);
                        }
                        g.drawImage(sprite.getImage(),
                                (int) getRealPos(gc, x + defaultW * i, y + defaultH * j).x, //dx1
                                (int) getRealPos(gc, x + defaultW * i, y + defaultH * j).y, //dy1 
                                (int) getRealPos(gc, x + defaultW * i + w, y + defaultH * j + h).x, //dx2
                                (int) getRealPos(gc, x + defaultW * i + w, y + defaultH * j + h).y, //dy2
                                0, //sx1
                                0, //sy1
                                w, //sx2
                                h, //
                                null);
                    }
                }
            }
        } else if (animation != null) {
            // Draw animation
            drawAnimation(gc, g);
        } else {
            // default look
            g.setColor(Color.black);
            g.fill(getOnScreenShape(gc));
        }

        // Selected part
        int xCenter = (int) getRealPos(gc, calculateCentroid().x, 0).x;
        int yCenter = (int) getRealPos(gc, 0, calculateCentroid().y).y;

        g.setColor(selected ? Color.red : Color.green);
        g.fillOval(xCenter - 5, yCenter - 5, 10, 10);

        if (selected) {
            int rX = (int) getRealPos(gc).x;
            int rY = (int) getRealPos(gc).y;
            int rW = (int) getRealWidth(gc);
            int rH = (int) getRealHeight(gc);
            g.setStroke(new BasicStroke(5));

            g.drawLine(rX, rY, rX + rW / 4, rY);
            g.drawLine(rX, rY, rX, rY + rH / 4);

            g.drawLine(rX + rW, rY, rX + rW - rW / 4, rY);
            g.drawLine(rX + rW, rY, rX + rW, rY + rH / 4);

            g.drawLine(rX + rW, rY + rH, rX + rW - rW / 4, rY + rH);
            g.drawLine(rX + rW, rY + rH, rX + rW, rY + rH - rH / 4);

            g.drawLine(rX, rY + rH, rX + rW / 4, rY + rH);
            g.drawLine(rX, rY + rH, rX, rY + rH - rH / 4);

            g.setStroke(new BasicStroke());

            // Square to resize the block
            g.drawRect((int) (getRealPos(gc).x + getRealWidth(gc)),
                    (int) (getRealPos(gc).y + getRealHeight(gc)),
                    (int) getRealWidth(gc, RESIZE_SQUARE_SIZE),
                    (int) getRealWidth(gc, RESIZE_SQUARE_SIZE));
        }
    }

    @Override
    public void drawAnimation(GameContainer gc, Graphics g) {
        if (animation == null) {
            gc.getEventHistory().addEvent("Couldn't draw animation because it is null");
            return;
        }

        //Restreint dans la taille du GameObject
        if (orientation == Orientation.RIGHT) {
            g.drawImage(animation.getSpriteToDraw(), (int) getRealPos(gc).x, (int) getRealPos(gc).y, (int) (width * gc.getCam().getScale()), (int) (height * gc.getCam().getScale()), null);
        } else {
            g.drawImage(animation.getSpriteToDraw(), (int) getRealPos(gc, x + width, y).x, (int) getRealPos(gc).y, (int) (-width * gc.getCam().getScale()), (int) (height * gc.getCam().getScale()), null);
        }

    }

    public void paste(Block copiedBlock) {
        width = copiedBlock.width;
        height = copiedBlock.height;
        sprite = copiedBlock.sprite;
        animation = copiedBlock.animation;
        platform = copiedBlock.platform;
        fixed = copiedBlock.fixed;
        stretchSprite = copiedBlock.stretchSprite;
        transparent = copiedBlock.transparent;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public int getMovingStartX() {
        return movingStartX;
    }

    public void setMovingStartX(int movingStartX) {
        this.movingStartX = movingStartX;
    }

    public int getMovingStartY() {
        return movingStartY;
    }

    public void setMovingStartY(int movingStartY) {
        this.movingStartY = movingStartY;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public boolean isPlatform() {
        return platform;
    }

    public void setPlatform(boolean platform) {
        this.platform = platform;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isStretchSprite() {
        return stretchSprite;
    }

    public void setStretchSprite(boolean stretchSprite) {
        this.stretchSprite = stretchSprite;
    }

    public boolean isResizing() {
        return resizing;
    }

    public void setResizing(boolean resizing) {
        this.resizing = resizing;
    }

}
