package com.roboxing.slicerextension.flow;

import java.io.IOException;

public class Slic3rPostProcessor extends AbstractPostProcessor {

    double minExtrusionLength = 0.3;  // Minimum extrusion length before we will allow a retraction
    double minTravelDistance = 0.01;  // Minimum distance of travel before we actually take the command seriously

    int layerCount = 0;

    String oldHint = "";
    String hint = "";
    double currentX = 0.00;
    double currentY = 0.00;
    double currentZ = 0.00;
    double commandDistance = 0.00;
    boolean outputZ = false;
    boolean layerChange = false;
    int currentSpeed = 0;
    int retractCount = 0;
    long travelMoveLastFileSize = 0;
    int lastMoveWasTravel = 0;
    int lastMoveWasRetract = 0;
    double extrusionAfterRetraction = 0;
    boolean retracted = false;


    String commandX = "false";
    String commandY = "false";
    String commandZ = "false";
    String commandE = "false";
    String commandSpeed = "false";
    String comment = "false";

    @Pattern("(X([\\-0-9\\.]+)\\s+Y([\\-0-9\\.]+))")
    private void getCommandDistance(String line, String all, double newX, double newY) {
        commandDistance = Math.sqrt(Math.pow(newX - currentX, 2) + Math.pow(newY - currentY, 2));
    }

    @Pattern("^(M204\\s+S(\\d+))")
    private void m201(String line, String all, String value) throws IOException {
        // printf NEW "M201 X%d Y%d Z%d E2000\n", $2, $2, $2;
        writeOutput(String.format("M201 X%d Y%d Z%d E2000\n", value, value, value));
    }

    @Pattern("^(M190\\s)")
    @Pattern("^(M104\\s)")
    @Pattern("^(M109\\s)")
    @Pattern("^(G21\\s)")
    @Pattern("^(G90\\s)")
    @Pattern("^(M83\\s)")
    @Pattern("^(M82\\s)")
    private void nothing() { }

    @Pattern("^(DISABLED_G1\\s+)")
    private void disabledG1(String line) throws IOException {
        // Grab all possible commands
        java.util.regex.Matcher m;
        if ((m = java.util.regex.Pattern.compile("(X([\\-0-9\\.]+)\\s)").matcher(line)).find()) {
            commandX = m.group(2);
        } else {
            commandX = "false";
        }
        if ((m = java.util.regex.Pattern.compile("(Y([\\-0-9\\.]+)\\s)").matcher(line)).find()) {
            commandY = m.group(2);
        } else {
            commandY = "false";
        }
        if ((m = java.util.regex.Pattern.compile("(Z([\\-0-9\\.]+)\\s)").matcher(line)).find()) {
            commandZ = m.group(2);
        } else {
            commandZ = "false";
        }
        if ((m = java.util.regex.Pattern.compile("(E([\\-0-9\\.]+)\\s)").matcher(line)).find()) {
            commandE = m.group(2);
        } else {
            commandE = "false";
        }
        if ((m = java.util.regex.Pattern.compile("(F([\\-0-9\\.]+)\\s)").matcher(line)).find()) {
            commandSpeed = m.group(2);
        } else {
            commandSpeed = "false";
        }
        if ((m = java.util.regex.Pattern.compile(";\\s+(.+)$").matcher(line)).find()) {
            comment = m.group(2);
        } else {
            comment = "false";
        }

        // Output hints
        if (comment.equals("skirt")) {
            hint = "SKIRT";
        }
        if (comment.equals("brim")) {
            hint = "SKIRT";
        }
        if (comment.equals("perimeter")) {
            hint = "WALL-OUTER";
        }
        if (comment.equals("infill")) {
            hint = "SKIN";
        }

        if (hint != oldHint) {
            writeOutput(";TYPE:" + hint + "\n");
            oldHint = hint;
            return;    // Break out of the if statement
        }

        if (commandSpeed != "false") {
        }
    }

    @Pattern("^(G1\\s+Z([0-9\\.]+)\\s+)")
    private void g1(String line, String all, double currentZ) throws IOException {
        // Layer change code
        if (retracted) {
            writeOutput(String.format("G0 X%s Y%s Z%s \n", currentX, currentY, currentZ));
        } else {
            outputZ = true;
            layerChange = true;
        }
        oldHint = "";
    }

    @Pattern("^(;LAYER:0)")
    private void layer0(String line) throws IOException {
        // Output the layer count
        writeOutput(String.format(";Layer count: %d\n", layerCount));
        writeOutput(line + "\n");
        extrusionAfterRetraction = 0;
    }

    @Pattern("^(;LAYER:)")
    private void layer(String line) throws IOException {
        // Output the layer number
        writeOutput(line + "\n");
        //$extrusionAfterRetraction = 0;
    }

    @Pattern("^(G1\\s+E([\\-0-9\\.]+)\\s+F([0-9]+))")
    private void retractionUnretraction(String line, String all, double extrusion, int feedRate) throws IOException {
        //retraction/unretraction

        // Don't print travel moves before a retraction
        if ((lastMoveWasTravel > 0) && (extrusion < 0)) {
            //TODO find a simple way to do the SEEK !!
            output.seek(travelMoveLastFileSize);
            //seek(NEW, travelMoveLastFileSize, 0);
        }

        // Ensure that we remove the first retract/unretract pair
        // Ensure that we leave enough room for slowly closing a nozzle before retracting
        if ((extrusionAfterRetraction > minExtrusionLength) || (retracted)) {
            writeOutput(String.format("G1 F%s E%s\n", feedRate, extrusion));
            retractCount++;

            if (extrusion > 0) {
                extrusionAfterRetraction = 0;
                retracted = false;
            } else {
                retracted = true;
            }
            lastMoveWasRetract = 2;
        }
    }

    @Pattern("^(G1\\s+F([\\-0-9]+))")
    private void currentSpeed(String line, String all, int currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    @Pattern("^(G1\\s+X([\\-0-9\\.]+)\\s+Y([\\-0-9\\.]+)\\sF([0-9]+))")
    private void showTravelMovesAsGo(String line, String all, String x, String y, String f) throws IOException {
        // Show travel moves as G0

        // Don't repeat travel moves
        if (lastMoveWasTravel > 0) {
            //TODO SEEK
            //seek(NEW, travelMoveLastFileSize, 0);
            output.seek(travelMoveLastFileSize);
        } else {
            //TODO TELL !!
            //travelMoveLastFileSize = tell(NEW);
            travelMoveLastFileSize = output.length();
        }

        if (commandDistance < minTravelDistance) {
            // Ignore, as this distance is too small for the postprocessor to handle
        } else {

            if (!retracted && !outputZ) {
                if (outputZ) {
                    writeOutput(String.format("G0 F%s X%s Y%s Z%s\n", f, x, y, currentZ));
                    outputZ = false;
                    extrusionAfterRetraction = 0;
                } else {
                    writeOutput(String.format("G0 F%s X%s Y%s E0.00\n", f, x, y));
                    lastMoveWasTravel = 2;
                }
            } else {
                if (outputZ) {
                    writeOutput(String.format("G0 F%s X%s Y%s Z%s\n", f, x, y, currentZ));
                    outputZ = false;
                } else {
                    writeOutput(String.format("G0 F%s X%s Y%s\n", f, x, y));
                }
                lastMoveWasTravel = 2;
            }
        }
    }

    @Pattern("^(G1\\s+X([\\-0-9\\.]+)\\s+Y([\\-0-9\\.]+)\\s+E([\\-0-9\\.]+)\\s+;\\s+(.+))$")
    private void outputHints(String list, String all, double x, double y, double e, String currentHint) throws IOException {
        // Output hints as to what is going on
        if (currentHint.equals("skirt"))
            hint = "SKIRT";
        if (currentHint.equals("brim"))
            hint = "SKIRT";
        if (currentHint.equals("perimeter"))
            hint = "WALL-OUTER";
        if (currentHint.equals("infill"))
            hint = "SKIN";
        if (currentHint.equals("support material"))
            hint = "SUPPORT";
        if (currentHint.equals("support material interface"))
            hint = "SUPPORT";

        if (!hint.equals(oldHint)) {
            writeOutput(String.format(";TYPE:%s\n", hint));
            oldHint = hint;
        }

        if ((x == currentX) && (y == currentY)) {
            // Don't output zero distance moves
        } else {

            if (currentSpeed == 0) {
                writeOutput(String.format("G1 X%s Y%s E%s \n", x, y, e));
                //printf NEW "G1 X%s Y%s E%s \n", $2, $3, $4;
            } else {
                writeOutput(String.format("G1 F%s X%s Y%s E%s \n", currentSpeed, x, y, e));
                currentSpeed = 0;
            }
            extrusionAfterRetraction += e;
        }
    }
    //    } else {
    //        System.out.println(strLine);
    //        //StandardCharsets.UTF_8.encode(strLine).array()
    //        writeOutput(strLine + "\n");
    //    }

    // Save the current position
    @Pattern("(X([\\-0-9\\.]+)\\s+Y([\\-0-9\\.]+))")
    private void saveCurrentPosition(String line, String all, double currentX, double currentY) {
        this.currentX = currentX;
        this.currentY = currentY;
    }

    @Override
    protected void afterLine(String line) {
        if (lastMoveWasTravel > 0) {
            lastMoveWasTravel--;
        }
        if (lastMoveWasRetract > 0) {
            lastMoveWasRetract--;
        }
    }
}
