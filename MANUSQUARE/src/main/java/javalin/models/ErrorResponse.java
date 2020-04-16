package javalin.models;

import java.util.Map;

public class ErrorResponse {
    public String title;
    public int status;
    public String type;
    public Map<String, String> details;
}