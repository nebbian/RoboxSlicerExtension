package com.roboxing.slicerextension.flow.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineProcessor {

    private Map<Pattern, VarStringMethod> defs = new LinkedHashMap<>();

    public LineProcessor() {
    }

    public void add(String regex, VarStringMethod method) {
        add(Pattern.compile(regex), method);
    }

    public void add(Pattern regex, VarStringMethod method) {
        defs.put(regex, method);
    }

    public boolean processLine(String line) {
        for (Map.Entry<Pattern, VarStringMethod> entry : defs.entrySet()) {
            Matcher matcher = entry.getKey().matcher(line);
            if (matcher.find()) {
                String[] args = new String[matcher.groupCount() + 1];
                for (int i = 0; i <= args.length; i++) {
                    args[i] = matcher.group(i);
                }
                entry.getValue().processLine(args);
            }
        }
        return false;
    }

    public static interface VarStringMethod {
        void processLine(String... args);
    }
}
