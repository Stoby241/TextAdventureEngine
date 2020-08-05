import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Utility {

    public static ArrayList<String> LoadFile(String localPath){
        ArrayList<String> lines = new ArrayList<>();
        String path = System.getProperty("user.dir") + localPath;
        try {
            File file = new File(path);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
               lines.add(line);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return lines;
    }

    public static JSONObject LoadJson(String localPath)  {
        JSONObject jo = null;
        String path = System.getProperty("user.dir") + localPath;
        try {
            jo  = (JSONObject) new JSONParser().parse(new FileReader(path));
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return jo;
    }
}
