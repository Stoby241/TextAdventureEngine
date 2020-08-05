import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.reflect.Type;
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

    Entity[] entities;
    Item[] items;
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

        JSONArray jEntities = (JSONArray) jo.get("Entities");
        entities = new Entity[jEntities.size()];
        for (int i = 0; i < jEntities.size(); i++){
            JSONObject jEntity = (JSONObject) jEntities.get(i);
            Entity entity = new Entity((String) jEntity.get("Name"), loadObservationTexts(jEntity));
            entities[i] = entity;
        }

        JSONArray jItems = (JSONArray) jo.get("Entities");
        items = new Item[jItems.size()];
        for (int i = 0; i < jItems.size(); i++){
            JSONObject jItem = (JSONObject) jItems.get(i);
            Item item = new Item((String) jItem.get("Name"), loadObservationTexts(jItem));
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

                JSONArray jEntitiesNames = (JSONArray) jPosition.get("Enities");
                Entity[] posEntities = new Entity[jEntities.size()];

                for (int k = 0; k < jEntitiesNames.size(); k++){
                    for (Entity entity :entities) {
                        if(entity.name.equals((String) jEntitiesNames.get(k))){
                            posEntities[k] = entity;
                            break;
                        }
                    }
                }

                Position position = new Position((String) jPosition.get("Name"), posEntities, loadObservationTexts(jPosition));
                positions[j] = position;
            }

            Scene scene = new Scene((String) jScene.get("Name"), positions);
            scenes[i] = scene;
        }

        for (Charcter character : charcters) {
            if(character.name.equals((String) jo.get("StartCharacter"))){
                currentCharcter = character;
            }
        }
    }

    private ObservationText[] loadObservationTexts(JSONObject jObject){

        JSONArray jObservationTexts = (JSONArray) jObject.get("ObservationTexts");
        ObservationText[] observationTexts = new ObservationText[jObservationTexts.size()];

        for (int t = 0; t < jObservationTexts.size(); t++){
            JSONObject jObservationText = (JSONObject) jObservationTexts.get(t);

            JSONArray jConditions = (JSONArray) jObservationText.get("Conditions");
            ObservationCondition[] observationConditions = new ObservationCondition[jConditions.size()];

            for (int c = 0; c < jConditions.size(); c++) {
                JSONObject jContition = (JSONObject) jConditions.get(c);

                ObservationCondition observationCondition = null;

                String name = (String) jContition.get("Name");
                switch (name){
                    case"Base":
                        observationCondition = new ObservationCondition();
                        break;
                    case"HasItem":
                        for (Item item : items) {
                            if(item.name.equals((String) jContition.get("ItemName"))){
                                observationCondition = new HasItemCondition(item);
                                break;
                            }
                        }
                        break;
                }

                observationConditions[c] = observationCondition;
            }

            ObservationText observationText = new ObservationText((String) jObservationText.get("Text"), observationConditions);
            observationTexts[t] = observationText;
        }
        return observationTexts;
    }

    private void loadInteractions(JSONObject jObject){
        JSONArray jInteractions = (JSONArray) jObject.get("Interactions");

        for (int t = 0; t < jInteractions.size(); t++){
            JSONObject jInteraction = (JSONObject) jInteractions.get(t);

            JSONArray jActions = (JSONArray) jInteraction.get("Actions");
            Action[] actions = new Action[jActions.size()];

            for (int c = 0; c < jActions.size(); c++) {
                String actionString = (String) jActions.get(c);
                String[] parts = actionString.split(":");

                Item item = null;
                for (Item testItem: items) {
                    if(testItem.name == parts[1]){
                        item = testItem;
                    }
                }

                Action action = null;

                switch (parts[0]){
                    case"ADDI":
                        action = new AddItemAction(item);
                        break;
                    case"RMI":
                        action = new RemoveItemAction(item);
                        break;
                }

                actions[c] = action;
            }

            Interaction interaction = new Interaction((String) jInteraction.get("Text"), actions);

            JSONArray jObjects = (JSONArray) jInteraction.get("Objects");

            for (int i = 0; i < 2; i++){
                String name = (String) jObjects.get(i);

                for (Item item : items){
                    if(item.name.equals(name)){
                        item.interactions.add(interaction);
                    }
                }

                for (Entity entity : entities){
                    if(entity.name.equals(name)){
                        entity.interactions.add(interaction);
                    }
                }
            }
        }
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
    ObservationText[] observationTexts;

    public Position(String name, Entity[] entities, ObservationText[] observationTexts) {
        this.name = name;
        this.entities = entities;
        this.observationTexts = observationTexts;
    }
}

class Entity {
    String name;
    ObservationText[] observationTexts;
    ArrayList<Interaction> interactions;

    public Entity(String name, ObservationText[] observationTexts) {
        this.name = name;
        this.observationTexts = observationTexts;

    }
}

class ObservationText {
    String text;
    ObservationCondition[] conditions;

    public ObservationText(String text, ObservationCondition[] conditions) {
        this.text = text;
        this.conditions = conditions;
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
        return Main.ins.currentCharcter.items.contains(item);
    }
}

class Item{
    String name;
    ObservationText[] observationTexts;
    ArrayList<Interaction> interactions;

    public Item(String name, ObservationText[] observationTexts) {
        this.name = name;
        this.observationTexts = observationTexts;
    }
}

class Interaction{
    String text;
    Action[] actions;

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

        Main.ins.currentCharcter.items.add(item);
    }
}

class RemoveItemAction extends Action{
    Item item;

    public RemoveItemAction(Item item) {
        this.item = item;
    }

    public void perform(){
        ArrayList<Item> removeItems = new ArrayList<>();
        for (Item testItem : Main.ins.currentCharcter.items) {
            if(item == testItem){
                removeItems.add(testItem);
            }
        }
        Main.ins.currentCharcter.items.removeAll(removeItems);
    }
}
