package validation;

import com.google.gson.Gson;

public class JSONValidator {
	
	// I am so sorry for this. FIXME: Hack warning
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
