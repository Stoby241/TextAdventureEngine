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

    Character currentCharacter;
    public Character getCurrentCharacter() {
        return currentCharacter;
    }

    Character[] characters;
    Entity[] entities;
    Item[] items;
    Scene[] scenes;

    public void LoadGame(){
        JSONObject jo = Utility.LoadJson("/resources/SaveGame.json");

        JSONArray jCharacters = (JSONArray) jo.get("Characters");
        characters = new Character[jCharacters.size()];
        for (int i = 0; i < jCharacters.size(); i++){
            JSONObject jCharacter = (JSONObject) jCharacters.get(i);
            Character character = new Character((String) jCharacter.get("Name"));
            characters[i] = character;
        }

        JSONArray jEntities = (JSONArray) jo.get("Entities");
        entities = new Entity[jEntities.size()];
        for (int i = 0; i < jEntities.size(); i++){
            JSONObject jEntity = (JSONObject) jEntities.get(i);
            Entity entity = new Entity((String) jEntity.get("Name"));
            entities[i] = entity;
        }

        JSONArray jItems = (JSONArray) jo.get("Items");
        items = new Item[jItems.size()];
        for (int i = 0; i < jItems.size(); i++){
            JSONObject jItem = (JSONObject) jItems.get(i);
            Item item = new Item((String) jItem.get("Name"));
            items[i] = item;
        }

        JSONArray jScenes = (JSONArray) jo.get("Scenes");
        scenes = new Scene[jScenes.size()];
        for (int i = 0; i < jScenes.size(); i++){

            JSONObject jScene = (JSONObject) jScenes.get(i);

            JSONArray jPositions = (JSONArray) jScene.get("Positions");
            Position[] positions = new Position[jPositions.size()];

            for (int j = 0; j < jPositions.size(); j++){

                JSONObject jPosition = (JSONObject) jPositions.get(j);

                JSONArray jEntitiesNames = (JSONArray) jPosition.get("Entities");
                Entity[] posEntities = new Entity[jEntities.size()];

                for (int k = 0; k < jEntitiesNames.size(); k++){
                    for (Entity entity :entities) {
                        if(entity.name.equals(jEntitiesNames.get(k))){
                            posEntities[k] = entity;
                            break;
                        }
                    }
                }

                Position position = new Position((String) jPosition.get("Name"), posEntities);
                position.observationTexts = loadObservationTexts(jPosition);
                positions[j] = position;
            }

            Scene scene = new Scene((String) jScene.get("Name"), positions);
            scenes[i] = scene;
        }

        for (Character character : characters) {
            if(character.name.equals(jo.get("StartCharacter"))){
                currentCharacter = character;
            }
        }

        for (int i = 0; i < jEntities.size(); i++){
            JSONObject jEntity = (JSONObject) jEntities.get(i);
            entities[i].observationTexts = loadObservationTexts(jEntity);
            loadInteractions(jEntity);
        }

        for (int i = 0; i < jItems.size(); i++){
            JSONObject jItem = (JSONObject) jItems.get(i);
            items[i].observationTexts = loadObservationTexts(jItem);
            loadInteractions(jItem);
        }

        for (int i = 0; i < jScenes.size(); i++){

            JSONObject jScene = (JSONObject) jScenes.get(i);
            Scene scene = scenes[i];
            JSONArray jPositions = (JSONArray) jScene.get("Positions");

            for (int j = 0; j < jPositions.size(); j++){

                JSONObject jPosition = (JSONObject) jPositions.get(j);
                Position position = scene.positions[j];
                JSONArray jLinks = (JSONArray) jPosition.get("PositionLinks");

                for (Object jLink : jLinks) {
                    String name = (String) jLink;
                    addPosLink(name, position);
                }
            }
        }

    }

    private ObservationText[] loadObservationTexts(JSONObject jObject){

        JSONArray jObservationTexts = (JSONArray) jObject.get("ObservationTexts");
        if(jObservationTexts == null) return new ObservationText[0];

        ObservationText[] observationTexts = new ObservationText[jObservationTexts.size()];

        for (int t = 0; t < jObservationTexts.size(); t++){
            JSONObject jObservationText = (JSONObject) jObservationTexts.get(t);

            JSONArray jConditions = (JSONArray) jObservationText.get("Conditions");

            ObservationText observationText;
            if(jConditions == null){
                observationText = new ObservationText((String) jObservationText.get("Text"));
            }
            else{

                ObservationCondition[] observationConditions = new ObservationCondition[jConditions.size()];

                for (int c = 0; c < jConditions.size(); c++) {
                    JSONObject jCondition = (JSONObject) jConditions.get(c);

                    ObservationCondition observationCondition = null;

                    String name = (String) jCondition.get("Name");
                    switch (name){
                        case"HasItem":
                            for (Item item : items) {
                                if(item.name.equals(jCondition.get("ItemName"))){
                                    observationCondition = new HasItemCondition(item);
                                    break;
                                }
                            }
                            break;
                    }
                    observationConditions[c] = observationCondition;
                }
                observationText = new ObservationText((String) jObservationText.get("Text"), observationConditions);
            }

            observationTexts[t] = observationText;
        }
        return observationTexts;
    }

    private void loadInteractions(JSONObject jObject){
        JSONArray jInteractions = (JSONArray) jObject.get("Interactions");
        if(jInteractions == null) return;

        for (Object o : jInteractions) {
            JSONObject jInteraction = (JSONObject) o;

            String text = (String) jInteraction.get("Text");

            JSONArray jActions = (JSONArray) jInteraction.get("Actions");
            Interaction interaction;
            if (jActions == null) {
                interaction = new Interaction(text);
            } else {
                Action[] actions = new Action[jActions.size()];

                for (int c = 0; c < jActions.size(); c++) {
                    String actionString = (String) jActions.get(c);
                    String[] parts = actionString.split(":");

                    Action action = null;

                    switch (parts[0]) {
                        case "ADDI":
                            Item item = null;
                            for (Item testItem : items) {
                                if (testItem.name.equals(parts[1])) {
                                    item = testItem;
                                }
                            }
                            action = new AddItemAction(item);
                            break;
                        case "RMI":
                            item = null;
                            for (Item testItem : items) {
                                if (testItem.name.equals(parts[1])) {
                                    item = testItem;
                                }
                            }
                            action = new RemoveItemAction(item);
                            break;
                    }

                    actions[c] = action;
                }

                if (text == null) {
                    interaction = new Interaction(actions);
                } else {
                    interaction = new Interaction(text, actions);
                }
            }

            JSONArray jObjects = (JSONArray) jInteraction.get("Objects");

            for (int i = 0; i < 2; i++) {
                boolean stop = false;
                String name = (String) jObjects.get(i);

                for (Item item : items) {
                    if (item.name.equals(name)) {
                        item.interactions.add(interaction);
                        stop = true;
                        break;
                    }
                }
                if (stop) break;

                for (Entity entity : entities) {
                    if (entity.name.equals(name)) {
                        entity.interactions.add(interaction);
                        break;
                    }
                }
            }
        }
    }

    private void addPosLink(String name, Position position){
        boolean stop = false;

        for (Scene testScene : scenes) {
            for (Position pos : testScene.positions) {
                if (pos.name.equals(name)) {
                    position.positionLinks.add(pos);
                    stop = true;
                    break;
                }
            }
            if (stop) break;
        }
    }
}

class Character {
    String name;
    ArrayList<Item> items;

    public Character(String name) {
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
    ObservationText[] observationTexts;
    ArrayList<Position> positionLinks;

    public Position(String name, Entity[] entities) {
        this.name = name;
        this.entities = entities;
        this.positionLinks = new ArrayList<>();
    }
}

class Entity {
    String name;
    ObservationText[] observationTexts;
    ArrayList<Interaction> interactions;

    public Entity(String name) {
        this.name = name;
        this.interactions = new ArrayList<>();
    }
}

class ObservationText {
    String text;
    ObservationCondition[] conditions;

    public ObservationText(String text, ObservationCondition[] conditions) {
        this.text = text;
        this.conditions = conditions;
    }
    public ObservationText(String text) {
        this.text = text;
        this.conditions = null;
    }
}

class ObservationCondition {

    public boolean isTrue(){
        return true;
    }
}

class HasItemCondition extends ObservationCondition {

    Item item;

    public HasItemCondition(Item item) {
        this.item = item;
    }

    public boolean isTrue(){
        return Main.ins.currentCharacter.items.contains(item);
    }
}

class Item{
    String name;
    ObservationText[] observationTexts;
    ArrayList<Interaction> interactions;

    public Item(String name) {
        this.name = name;
        this.interactions = new ArrayList<>();
    }
}

class Interaction{
    String text;
    Action[] actions;

    public Interaction(String text) {
        this.text = text;
        this.actions = null;
    }

    public Interaction(Action[] actions) {
        this.text = null;
        this.actions = actions;
    }

    public Interaction(String text, Action[] actions) {
        this.text = text;
        this.actions = actions;
    }
}

class Action{
    public void perform(){ }
}

class AddItemAction extends Action{
    Item item;

    public AddItemAction(Item item) {
        this.item = item;
    }

    public void perform(){

        Main.ins.currentCharacter.items.add(item);
    }
}

class RemoveItemAction extends Action{
    Item item;

    public RemoveItemAction(Item item) {
        this.item = item;
    }

    public void perform(){
        ArrayList<Item> removeItems = new ArrayList<>();
        for (Item testItem : Main.ins.currentCharacter.items) {
            if(item == testItem){
                removeItems.add(testItem);
            }
        }
        Main.ins.currentCharacter.items.removeAll(removeItems);
    }
}
