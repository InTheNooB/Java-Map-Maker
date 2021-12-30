package app.creator;

import engine.GameContainer;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Map implements Serializable {

    private HashMap<Integer, CopyOnWriteArrayList<Block>> blocks;
    private HashMap<String, String> parameters;
    private GameContainer gc;

    public Map(GameContainer gc) {
        this.gc = gc;
    }

    public Map(HashMap<Integer, CopyOnWriteArrayList<Block>> blocks) {
        this.blocks = blocks;
        parameters = new HashMap();
    }

    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }

    public HashMap<Integer, CopyOnWriteArrayList<Block>> getBlocks() {
        return blocks;
    }

}
