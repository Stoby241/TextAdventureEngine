import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Utility {

    static Scanner in = new Scanner(System.in);

    public static JSONObject loadJson(String localPath)  {
        JSONObject jo = null;
        String path = System.getProperty("user.dir") + localPath;
        try {
            jo  = (JSONObject) new JSONParser().parse(new FileReader(path));
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return jo;
    }

    public static void print(String msg){
        System.out.println(msg);
    }

    public static String getInput(){
        return in.nextLine();
    }

}
