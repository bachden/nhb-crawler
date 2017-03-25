package nhb.utils;

import java.util.Map;
import java.util.Map.Entry;

public class StringTemplate {

	public static final String replace(String template, Map<String, String> params) {
		String result = template;
		for (Entry<String, String> entry : params.entrySet()) {
			result = result.replaceAll("\\{\\{" + entry.getKey() + "\\}\\}", entry.getValue());
		}
		return result;
	}
}
