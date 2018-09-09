package com.example.lakshaysharma.instaclone.Utils;

public class StringManipulation {

    public static String expandUsername(String username){
        return username.replace(".", " ");
    }

    public static String condenseUsername(String username){
        return username.replace(" ", ".");
    }

    public static String getTags(String description){

        if (description.indexOf('#') > 0){
            StringBuilder stringBuilder = new StringBuilder();
            char[] chars = description.toCharArray();
            boolean foundWord = false;
            for (char c: chars){
                if (c == '#'){
                    foundWord = true;
                    stringBuilder.append(c);
                }
                else {
                    if (foundWord){
                        stringBuilder.append(c);
                    }
                }

                if (c == ' '){
                    foundWord = false;
                }
            }

            String tags = stringBuilder.toString().replace(" ", "").replace("#", " #");
            return tags.substring(1, tags.length());
        }
        return description;

    }

}
