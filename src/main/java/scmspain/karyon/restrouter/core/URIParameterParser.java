package scmspain.karyon.restrouter.core;


import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class URIParameterParser {

  private final String regexPattern = "(\\{.*?\\})";
  private final String regexAllthethings = "([^\\?\\/]+)";
  private final String regexEnd = "\\/?$";


  public Map<String, String> getParams(String pathUri, String requestUri) {

    requestUri = requestUri.split("\\?")[0]; //REMOVE FROM THE URI ALL QUERY PARAMS
    String requestUriPattern = this.getUriRegex(pathUri);
    Map<String, String> allMatches = new HashMap<String, String>();

    Matcher m = Pattern.compile(requestUriPattern).matcher(requestUri);
    Matcher mKeys = Pattern.compile(requestUriPattern).matcher(pathUri);

    while (m.find() && mKeys.find()) {
      for (int i = 1; i <= m.groupCount(); i++) {
        String paramKey = mKeys.group(i);
        paramKey = paramKey.replaceAll("[{}]", "");
        allMatches.put(paramKey, m.group(i));
      }

    }

    return allMatches;
  }

  public String getUriRegex(String pathUri) {

    return pathUri.replaceAll(regexPattern, regexAllthethings)+regexEnd;
  }

}
