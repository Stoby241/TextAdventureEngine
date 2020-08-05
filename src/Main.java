import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Main {

    static Main ins;

    public static Main getIns() {
        return ins;
    }

    public Main() {
        ins = this;

        LoadGame();
    }

    Charcter[] charcters;
    Charcter currentCharcter;

    Scene[] scenes;

    public Charcter getCurrentCharcter() {
        return currentCharcter;
    }

    public void LoadGame(){
        JSONObject jo = Utility.LoadJson("/resources/SaveGame.json");

        JSONArray jCharacters = (JSONArray) jo.get("Characters");
        charcters = new Charcter[jCharacters.size()];

        for (int i = 0; i < jCharacters.size(); i++){
            JSONObject jCharacter = (JSONObject) jCharacters.get(i);
            Charcter charcter = new Charcter((String) jCharacter.get("Name"));
            charcters[i] = charcter;
        }

        JSONArray jScenes = (JSONArray) jo.get("Scenes");
        scenes = new Scene[jScenes.size()];

        for (int i = 0; i < jScenes.size(); i++){

            JSONObject jScene = (JSONObject) jScenes.get(i);

            JSONArray jPositions = (JSONArray) jScene.get("Positions");
            Position[] positions = new Position[jPositions.size()];

            for (int j = 0; j < jPositions.size(); j++){

                JSONObject jPosition = (JSONObject) jPositions.get(j);

                JSONArray jEntities = (JSONArray) jPosition.get("Enities");
                Entity[] entities = new Entity[jEntities.size()];

                for (int k = 0; k < jEntities.size(); k++){
                    JSONObject jEntity = (JSONObject) jEntities.get(k);

                    Entity entity = new Entity((String) jEntity.get("Name"), loadTextOutputs(jEntity));
                    entities[k] = entity;
                }

                Position position = new Position((String) jPosition.get("Name"), entities, loadTextOutputs(jPosition));
                positions[j] = position;
            }

            Scene scene = new Scene((String) jScene.get("Name"), positions);
            scenes[i] = scene;
        }
    }

    private TextOutput[] loadTextOutputs(JSONObject jObject){

        JSONArray jTextouts = (JSONArray) jObject.get("TextOut");
        TextOutput[] textOutputs = new TextOutput[jTextouts.size()];

        for (int t = 0; t < jTextouts.size(); t++){
            JSONObject jTextout = (JSONObject) jTextouts.get(t);

            JSONArray jConditions = (JSONArray) jTextout.get("Conditions");
            TextCondition[] textConditions = new TextCondition[jConditions.size()];

            for (int c = 0; c < jConditions.size(); c++) {
                JSONObject jContition = (JSONObject) jConditions.get(c);
                TextCondition textCondition = new TextCondition((String) jContition.get("Name"));
                textConditions[c] = textCondition;
            }

            TextOutput textOutput = new TextOutput((String) jTextout.get("Text"), textConditions);
            textOutputs[t] = textOutput;
        }
        return textOutputs;
    }
}

class Charcter{
    String name;
    ArrayList<Item> items;

    public Charcter(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }
}

class Scene{
    String name;
    Position[] positions;

    public Scene(String name, Position[] positions) {
        this.name = name;
        this.positions = positions;
    }
}

class Position{
    String name;
    Entity[] entities;
    TextOutput[] textOutputs;

    public Position(String name, Entity[] entities, TextOutput[] textOutputs) {
        this.name = name;
        this.entities = entities;
        this.textOutputs = textOutputs;
    }
}

class Entity {
    String name;
    TextOutput[] textOutputs;

    public Entity(String name, TextOutput[] textOutputs) {
        this.name = name;
        this.textOutputs = textOutputs;
    }
}

class Item{
    String name;
    TextOutput[] textOutputs;
}

class TextOutput{
    String text;
    TextCondition[] conditions;

    public TextOutput(String text, TextCondition[] conditions) {
        this.text = text;
        this.conditions = conditions;
    }
}

class TextCondition{
    String name;

    public TextCondition(String name) {
        this.name = name;
    }

    public boolean isTrue(){
        return true;
    }
}

class HasItemCondition extends TextCondition{

    Item item;

    public HasItemCondition(String name, Item item) {
        super(name);
        this.item = item;
    }

    public boolean isTrue(){
        return Main.ins.currentCharcter.items.contains(item);
    }
}