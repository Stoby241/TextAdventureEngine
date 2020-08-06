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

        loadGame();

        run();
    }

    Character[] characters;
    Scene[] scenes;

    Character currentCharacter;
    Scene currentScene;
    Position currentPosition;

    public void loadGame(){
        JSONObject jo = Utility.loadJson("/resources/SaveGame.json");

        JSONArray jCharacters = (JSONArray) jo.get("Characters");
        characters = new Character[jCharacters.size()];
        for (int i = 0; i < jCharacters.size(); i++){
            JSONObject jCharacter = (JSONObject) jCharacters.get(i);
            Character character = new Character((String) jCharacter.get("Id"), (String) jCharacter.get("Name"));
            characters[i] = character;
        }

        JSONArray jScenes = (JSONArray) jo.get("Scenes");
        scenes = new Scene[jScenes.size()];
        for (int i = 0; i < jScenes.size(); i++){

            JSONObject jScene = (JSONObject) jScenes.get(i);

            JSONArray jEntities = (JSONArray) jScene.get("Entities");
            Entity[] entities = new Entity[jEntities.size()];
            for (int j = 0; j < jEntities.size(); j++){
                JSONObject jEntity = (JSONObject) jEntities.get(j);
                Entity entity = new Entity((String) jEntity.get("Id"), (String) jEntity.get("Name"));
                entities[j] = entity;
            }

            JSONArray jItems = (JSONArray) jScene.get("Items");
            Item[] items = new Item[jItems.size()];
            for (int j = 0; j < jItems.size(); j++){
                JSONObject jItem = (JSONObject) jItems.get(j);
                Item item = new Item((String) jItem.get("Id"), (String) jItem.get("Name"));
                items[j] = item;
            }

            JSONArray jPositions = (JSONArray) jScene.get("Positions");
            Position[] positions = new Position[jPositions.size()];
            for (int j = 0; j < jPositions.size(); j++){

                JSONObject jPosition = (JSONObject) jPositions.get(j);

                JSONArray jEntityIds = (JSONArray) jPosition.get("Entities");
                ArrayList<Entity> posEntities = new ArrayList<>();

                for (int k = 0; k < jEntityIds.size(); k++){
                    for (Entity entity : entities) {
                        if(entity.id.equals(jEntityIds.get(k))){
                            posEntities.add(entity);
                            break;
                        }
                    }
                }

                Position position = new Position((String) jPosition.get("Id"), (String) jPosition.get("Name"), posEntities);
                positions[j] = position;
            }

            Scene scene = new Scene((String) jScene.get("Id"), (String) jScene.get("Name"), positions, entities, items);
            scenes[i] = scene;

            for (int j = 0; j < jEntities.size(); j++){
                JSONObject jEntity = (JSONObject) jEntities.get(j);
                entities[j].observationTexts = loadObservationTexts(jEntity,scene);
                loadInteractions(jEntity,scene);
            }

            for (int j = 0; j < jItems.size(); j++){
                JSONObject jItem = (JSONObject) jItems.get(j);
                items[j].observationTexts = loadObservationTexts(jItem,scene);
                loadInteractions(jItem,scene);
            }

            for (int j = 0; j < jPositions.size(); j++){


                JSONObject jPosition = (JSONObject) jPositions.get(j);
                Position position = scene.positions[j];
                position.observationTexts = loadObservationTexts(jPosition,scene);

                JSONArray jLinks = (JSONArray) jPosition.get("PositionLinks");

                for (Object jLink : jLinks) {
                    String id = (String) jLink;
                    addPosLink(id, position);
                }
            }
        }

        for (Character character : characters) {
            if(character.id.equals(jo.get("StartCharacter"))){
                currentCharacter = character;
            }
        }

        for (Scene scene : scenes) {
            if(scene.id.equals(jo.get("StartScene"))){
                currentScene = scene;
                for (Position position : scene.positions) {
                    if(position.id.equals(jo.get("StartPosition"))) {
                        currentPosition = position;
                        break;
                    }
                }
                break;
            }
        }

        running = true;
    }

    private ObservationText[] loadObservationTexts(JSONObject jObject, Scene scene){

        JSONArray jObservationTexts = (JSONArray) jObject.get("ObservationTexts");
        if(jObservationTexts == null) return new ObservationText[0];

        ObservationText[] observationTexts = new ObservationText[jObservationTexts.size()];

        for (int t = 0; t < jObservationTexts.size(); t++){
            JSONObject jObservationText = (JSONObject) jObservationTexts.get(t);

            ObservationText observationText = new ObservationText((String) jObservationText.get("Text"), loadContitions(jObject, jObservationText, scene));
            observationTexts[t] = observationText;
        }
        return observationTexts;
    }
    private void loadInteractions(JSONObject jObject, Scene scene){
        JSONArray jInteractions = (JSONArray) jObject.get("Interactions");
        if(jInteractions == null) return;

        for (Object o : jInteractions) {
            JSONObject jInteraction = (JSONObject) o;

            String text = (String) jInteraction.get("Text");

            JSONArray jActions = (JSONArray) jInteraction.get("Actions");
            Action[] actions = null;
            if (jActions != null){
                actions = new Action[jActions.size()];

                for (int c = 0; c < jActions.size(); c++) {
                    String actionString = (String) jActions.get(c);
                    String[] parts = actionString.split(":");

                    Action action = null;

                    switch (parts[0]) {
                        case "ADDI":
                            Item item = null;
                            for (Item testItem : scene.items) {
                                if (testItem.id.equals(parts[1])) {
                                    item = testItem;
                                    break;
                                }
                            }
                            action = new AddItemAction(item);
                            break;
                        case "RMI":
                            item = null;
                            for (Item testItem : scene.items) {
                                if (testItem.id.equals(parts[1])) {
                                    item = testItem;
                                    break;
                                }
                            }
                            action = new RemoveItemAction(item);
                            break;
                        case "F":
                            Entity entity = null;
                            for (Entity testentity : scene.entities) {
                                if (testentity.id.equals((String) jObject.get("Id"))) {
                                    entity = testentity;
                                    break;
                                }
                            }
                            String flag = "";
                            if (parts.length > 1){
                                flag = parts[1];
                            }

                            action = new SetFlagAction(flag, entity);
                            break;
                        case "ADDE":
                            entity = null;
                            for (Entity testentity : scene.entities) {
                                if (testentity.id.equals(parts[1])) {
                                    entity = testentity;
                                    break;
                                }
                            }
                            Position position = null;
                            for (Position testposition : scene.positions) {
                                if (testposition.id.equals(parts[2])) {
                                    position = testposition;
                                    break;
                                }
                            }
                            action = new AddEntityAction(position, entity);
                            break;
                        case "RME":
                            entity = null;
                            for (Entity testentity : scene.entities) {
                                if (testentity.id.equals(parts[1])) {
                                    entity = testentity;
                                    break;
                                }
                            }
                            position = null;
                            for (Position testposition : scene.positions) {
                                if (testposition.id.equals(parts[2])) {
                                    position = testposition;
                                    break;
                                }
                            }
                            action = new RemoveEntityAction(position, entity);
                            break;
                    }

                    actions[c] = action;
                }
            }

            Interaction interaction = new Interaction(text, actions, loadContitions(jObject, jInteraction, scene));

            JSONArray jObjects = (JSONArray) jInteraction.get("Objects");
            interaction.objects = new Object[jObjects.size()];

            for (int i = 0; i < jObjects.size(); i++) {
                boolean stop = false;
                String id = (String) jObjects.get(i);

                for (Item item : scene.items) {
                    if (item.id.equals(id)) {
                        item.interactions.add(interaction);
                        interaction.objects[i] = item;
                        stop = true;
                        break;
                    }
                }
                if (stop) continue;

                for (Entity entity : scene.entities) {
                    if (entity.id.equals(id)) {
                        entity.interactions.add(interaction);
                        interaction.objects[i] = entity;
                        break;
                    }
                }
            }
        }
    }
    private void addPosLink(String id, Position position){
        boolean stop = false;

        for (Scene testScene : scenes) {
            for (Position pos : testScene.positions) {
                if (pos.id.equals(id)) {
                    position.positionLinks.add(pos);
                    stop = true;
                    break;
                }
            }
            if (stop) break;
        }
    }
    private GameCondition[] loadContitions(JSONObject jRoot, JSONObject jObject, Scene scene){
        JSONArray jConditions = (JSONArray) jObject.get("Conditions");
        GameCondition[] conditions = null;
        if (jConditions != null) {
            conditions = new GameCondition[jConditions.size()];
            for (int c = 0; c < jConditions.size(); c++) {
                String conditionString = (String) jConditions.get(c);
                String[] parts = conditionString.split(":");

                GameCondition condition = null;

                switch (parts[0]) {
                    case "HF":
                        Entity entity = null;
                        for (Entity testentity : scene.entities) {
                            if (testentity.id.equals((String) jRoot.get("Id"))) {
                                entity = testentity;
                                break;
                            }
                        }
                        condition = new HasFlagGameCondition(entity, parts[1]);
                        break;
                    case "NF":
                        entity = null;
                        for (Entity testentity : scene.entities) {
                            if (testentity.id.equals((String) jRoot.get("Id"))) {
                                entity = testentity;
                                break;
                            }
                        }
                        condition = new HasNotFlagGameCondition(entity, parts[1]);
                        break;
                    case "HI":
                        Item item = null;
                        for (Item testItem : scene.items) {
                            if (testItem.id.equals(parts[1])) {
                                item = testItem;
                                break;
                            }
                        }
                        condition = new HasItemGameCondition(item);
                        break;
                    case "NI":
                        item = null;
                        for (Item testItem : scene.items) {
                            if (testItem.id.equals(parts[1])) {
                                item = testItem;
                                break;
                            }
                        }
                        condition = new HasNotItemGameCondition(item);
                        break;
                }

                conditions[c] = condition;
            }

        }
        return conditions;
    }

    boolean running;
    private void run(){

        observe(currentPosition.observationTexts);

        while (running){
            String input = Utility.getInput();
            String[] inputParts = input.split(" ");

            switch (inputParts[0]){
                case "Beobachte":
                    boolean printedtext = false;
                    Object object = findObject(inputParts[1]);

                    if (object == null){
                        Utility.print("Kein Object.");
                        printedtext = true;
                    }
                    else if (object.getClass().equals(Item.class)){
                        printedtext = observe(((Item) object).observationTexts);
                    }
                    else if (object.getClass().equals(Entity.class)){
                        printedtext = observe(((Entity) object).observationTexts);
                    }
                    else if (object.getClass().equals(Position.class)){
                        printedtext = observe(((Position) object).observationTexts);
                    }else {
                        Utility.print("Kein Object.");
                        printedtext = true;
                    }

                    if(!printedtext){
                        Utility.print("Hier gibt es nichts zu sehen.");
                    }

                    break;

                case "Benutze":
                    printedtext = false;

                    object = findObject(inputParts[1]);
                    Object object2 = null;
                    if (inputParts.length > 2){
                        object2 = findObject(inputParts[2]);
                    }

                    if (object == null){
                        Utility.print("Kein Object.");
                        printedtext = true;
                    }
                    else if (object.getClass().equals(Item.class)){
                        Item item = (Item) object;
                        for (Interaction interaction : item.interactions) {
                            if(object2 == null && interaction.objects.length == 1){
                                printedtext = interact(interaction);
                                if(printedtext) break;
                            }
                            else if(interaction.objects.length == 2){
                                if (object2 != null && (interaction.objects[0].getClass().equals(object.getClass()) && interaction.objects[1].getClass().equals(object2.getClass()) ||
                                        interaction.objects[1].getClass().equals(object.getClass()) && interaction.objects[0].getClass().equals(object2.getClass()))) {
                                    printedtext = interact(interaction);
                                    if (printedtext) break;
                                }
                            }
                        }
                    }
                    else if (object.getClass().equals(Entity.class)){
                        Entity entity = (Entity) object;
                        for (Interaction interaction : entity.interactions) {
                            if(object2 == null && interaction.objects.length == 1){
                                printedtext = interact(interaction);
                                if(printedtext) break;
                            }
                            else if(interaction.objects.length == 2){
                                if (object2 != null && (interaction.objects[0].getClass().equals(object.getClass()) && interaction.objects[1].getClass().equals(object2.getClass()) ||
                                        interaction.objects[1].getClass().equals(object.getClass()) && interaction.objects[0].getClass().equals(object2.getClass()))) {
                                    printedtext = interact(interaction);
                                    if (printedtext) break;
                                }
                            }
                        }
                    }
                    else {
                        Utility.print("Kein Object.");
                        printedtext = true;
                    }
                    if(!printedtext){
                        Utility.print("Das geht nicht!");
                    }

                    break;

                case "Gehe":

                    object = findObject(inputParts[1]);

                    if (object == null){
                        Utility.print("Kein Ort.");
                    }
                    else if (object.getClass().equals(Position.class)){
                        Position position= (Position) object;
                        for (Position posLink : currentPosition.positionLinks) {
                            if(posLink == position){
                                currentPosition = position;
                                observe(position.observationTexts);
                                break;
                            }
                        }
                    }else {
                        Utility.print("Kein Ort.");
                    }

                    break;
                case "Umschauen":
                    Utility.print("Hier bin ich: " + currentPosition.name);

                    if(currentPosition.entities.size() != 0) {
                        Utility.print("---Objekte---");
                        for (Entity enity : currentPosition.entities) {
                            Utility.print(enity.name);
                        }
                    }
                    if(!currentPosition.positionLinks.isEmpty()){
                        Utility.print("---Wege---");
                        for (Position position : currentPosition.positionLinks) {
                            Utility.print(position.name);
                        }
                    }
                    if(!currentCharacter.items.isEmpty()){
                        Utility.print("---Inventar---");
                        for (Item item : currentCharacter.items) {
                            Utility.print(item.name);
                        }
                    }
                    break;
                case "Hilfe":
                    Utility.print("Befele sind:\nBeobachte <Objekt oder Ort> \nBenutze <Objekt> \nBenutze <Objekt> <Objekt> \nGehe <Ort> \nUmschauen \nHilfe");
                    break;
                default:
                    Utility.print("Kein Befehls Wort.");
                    break;
            }
        }
    }
    private Object findObject(String name){
        for (Item item : currentCharacter.items) {
            if (item.name.equals(name)){
                return item;
            }
        }
        for (Entity entity : currentScene.entities) {
            if (entity.name.equals(name)){
                return entity;
            }
        }
        for (Position position : currentScene.positions) {
            if (position.name.equals(name)){
                return position;
            }
        }
        return null;
    }
    private boolean observe(ObservationText[] observationTexts){
        for (ObservationText observationText : observationTexts){

            boolean print = true;
            if(observationText.gameConditions != null){
                for (GameCondition condition : observationText.gameConditions) {
                    if (!condition.isTrue()) print = false;
                }
            }

            if(print){
                Utility.print(observationText.text);
                return true;
            }
        }
        return false;
    }
    private boolean interact(Interaction interaction){
        boolean perform = true;
        if(interaction.conditions != null){
            for (GameCondition condition : interaction.conditions) {
                if (!condition.isTrue()) perform = false;
            }
        }

        if(perform){
            if(interaction.text != null){
                Utility.print(interaction.text);
            }
            if(interaction.actions != null){
                for (Action action : interaction.actions) {
                    action.perform();
                }
            }
        }
        return perform;
    }
}

class Character {
    String id;
    String name;
    ArrayList<Item> items;

    public Character(String id, String name) {
        this.id = id;
        this.name = name;
        this.items = new ArrayList<>();
    }
}

class Scene{
    String id;
    String name;
    Position[] positions;
    Entity[] entities;
    Item[] items;

    public Scene(String id, String name, Position[] positions, Entity[] entities, Item[] items) {
        this.id = id;
        this.name = name;
        this.positions = positions;
        this.entities = entities;
        this.items = items;
    }
}

class Position{
    String id;
    String name;
    ArrayList<Entity> entities;
    ObservationText[] observationTexts;
    ArrayList<Position> positionLinks;

    public Position(String id, String name, ArrayList<Entity> entities) {
        this.id = id;
        this.name = name;
        this.entities = entities;
        this.positionLinks = new ArrayList<>();
    }
}

class Entity {
    String id;
    String name;
    String flag;
    ObservationText[] observationTexts;
    ArrayList<Interaction> interactions;

    public Entity(String id, String name) {
        this.id = id;
        this.name = name;
        this.interactions = new ArrayList<>();
    }
}

class ObservationText {
    String text;
    GameCondition[] gameConditions;

    public ObservationText(String text, GameCondition[] gameConditions) {
        this.text = text;
        this.gameConditions = gameConditions;
    }
    public ObservationText(String text) {
        this.text = text;
        this.gameConditions = null;
    }
}

class GameCondition {

    public boolean isTrue(){
        return true;
    }
}

class HasFlagGameCondition extends GameCondition {
    Entity entity;
    String flag;

    public HasFlagGameCondition(Entity entity, String flag) {
        this.entity = entity;
        this.flag = flag;
    }

    public boolean isTrue(){
        return entity.flag.equals(flag);
    }
}
class HasNotFlagGameCondition extends GameCondition {
    Entity entity;
    String flag;

    public HasNotFlagGameCondition(Entity entity, String flag) {
        this.entity = entity;
        this.flag = flag;
    }

    public boolean isTrue(){
        return entity.flag == null || !entity.flag.equals(flag);
    }
}

class HasItemGameCondition extends GameCondition {

    Item item;

    public HasItemGameCondition(Item item) {
        this.item = item;
    }

    public boolean isTrue(){
        return Main.ins.currentCharacter.items.contains(item);
    }
}
class HasNotItemGameCondition extends GameCondition {

    Item item;

    public HasNotItemGameCondition(Item item) {
        this.item = item;
    }

    public boolean isTrue(){
        return !Main.ins.currentCharacter.items.contains(item);
    }
}

class Item{
    String id;
    String name;
    ObservationText[] observationTexts;
    ArrayList<Interaction> interactions;

    public Item(String id, String name) {
        this.id = id;
        this.name = name;
        this.interactions = new ArrayList<>();
    }
}

class Interaction{
    String text;
    Action[] actions;
    GameCondition[] conditions;
    Object[] objects;

    public Interaction(String text, Action[] actions, GameCondition[] conditions) {
        this.text = text;
        this.actions = actions;
        this.conditions = conditions;
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
        Utility.print(item.name +" Zum Inventar hinzugefügt.");
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
        Utility.print(item.name +" Aus Inventar entfernt.");
    }
}

class SetFlagAction extends Action{

    String flag;
    Entity entity;

    public SetFlagAction(String flag, Entity entity) {
        this.flag = flag;
        this.entity = entity;
    }

    public void perform(){
        entity.flag = flag;
    }
}

class AddEntityAction extends Action{
    Position position;
    Entity entity;

    public AddEntityAction(Position position, Entity entity) {
        this.position = position;
        this.entity = entity;
    }

    public void perform(){
        position.entities.add(entity);
    }
}

class RemoveEntityAction extends Action{
    Position position;
    Entity entity;

    public RemoveEntityAction(Position position, Entity entity) {
        this.position = position;
        this.entity = entity;
    }

    public void perform(){
        position.entities.remove(entity);
    }
}
