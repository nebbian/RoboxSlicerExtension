package com.roboxing.slicerextension.flow.utils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTut3 {

    public static void main(String args[]) {
        String line = "This order was placed for QT3000! OK?";
        String pattern = "(.*?)(\\d+)(.*)";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(line);

        if (m.find()) {
            for (int i = 0; i <= m.groupCount(); i++) {
                System.out.println("group(" + i + "): " + m.group(i));
            }
        } else {
            System.out.println("NO MATCH");
        }
    }

}