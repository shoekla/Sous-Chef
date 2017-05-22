package com.example.abirshukla.souschef;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by abirshukla on 5/18/17.
 */

public class DataForUser {
    public static ArrayList<String> urls = new ArrayList<>();
    public static ArrayList<String> vids = new ArrayList<>();
    public static ArrayList<String> vidTitles = new ArrayList<>();
    public static ArrayList<String> ingredients = new ArrayList<>();
    public static ArrayList<String> times = new ArrayList<>();
    public static ArrayList<String> directions = new ArrayList<>();
    public static ArrayList<String> favorites = new ArrayList<>();
    public static String cals = "";
    public static String serve = "";
    public static String everything = "";
    public static int step = 0;
    public static HashMap hm = new HashMap();


    public static ArrayList<String> search = new ArrayList<>();
    public static ArrayList<String> sousPages = new ArrayList<>();
    public static ArrayList<String> sousTitles = new ArrayList<>();
    public static String dishName = "";
    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        DataForUser.email = email;
        DataForUser.firebaseName = email.substring(0,email.indexOf("@")).replace(".","");
    }
    public static String editFav(String s) {
        if (favorites.contains(s)) {
            favorites.remove(s);
            return "Removed Dish from Favorites";
        }
        else {
            favorites.add(s);
            return "Added Dish to Favorites";
        }
    }

    public static String email = "";
    private static String firebaseName = "";
    public static String getFirbaseName() {
        return firebaseName;
    }
}
