package validation;

import com.google.gson.Gson;

public class JSONValidation {
	
	// I am so sorry for this. TODO: Hack warning
	public static boolean isJSONValid(String jsonInString) {
		Gson gson = new Gson();
		try {
			gson.fromJson(jsonInString, Object.class);
			return true;
		} catch (com.google.gson.JsonSyntaxException ex) {
			return false;
		}
	}

}
