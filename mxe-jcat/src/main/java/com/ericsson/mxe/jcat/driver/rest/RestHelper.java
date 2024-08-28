package com.ericsson.mxe.jcat.driver.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.http.ResponseEntity;
import java.util.Map;

public class RestHelper {
    public static boolean checkStatusCodeIsSuccessful(ResponseEntity<?> response) {
        return response.getStatusCode().is2xxSuccessful();
    }

    public static boolean checkStatusCode(ResponseEntity<?> response, int expect) {
        return response.getStatusCode().value() == expect;
    }

    public static boolean checkBodyIsNotNull(ResponseEntity<?> response) {
        return response.getBody() != null;
    }

    public static boolean fullCheck(ResponseEntity<?> response) {
        return checkStatusCodeIsSuccessful(response) && checkBodyIsNotNull(response);
    }

    static Map<String, String> getMapFromString(String response) {
        return new Gson().fromJson(response, new TypeToken<Map<String, String>>() {}.getType());
    }

    static String link(String... strings) {
        return String.join("/", strings);
    }
}
