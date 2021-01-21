package json;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;


public class TestJSON {
	
	public static void main(String[] args) {
		
		Map<Integer, String> colours = new HashMap<>();
        
        Gson gson = new Gson();
        
        String output = gson.toJson(colours);
        
        System.out.println(output);
		
	}

}
