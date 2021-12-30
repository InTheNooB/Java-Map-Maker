package app;

import app.creator.MapMaker;
import engine.GameContainer;

public class Main {

    public static void main(String[] args) {
        GameContainer gc = new GameContainer(new MapMaker());
        gc.start();
    }

}
