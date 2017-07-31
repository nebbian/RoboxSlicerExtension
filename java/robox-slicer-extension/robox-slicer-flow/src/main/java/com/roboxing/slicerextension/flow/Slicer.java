/*
 * This file is part of Robox Slicer Extension.
 *
 * Robox Slicer Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robox Slicer Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robox Slicer Extension.  If not, see <http:// www.gnu.org/licenses/>.
 *
*/
package com.roboxing.slicerextension.flow;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

/**
 * This is base class for various slicers.
 *
 */
public abstract class Slicer {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private String name;
    private Arguments args;
    private JSONObject slicerConfig;

    protected int layerCount=0;
    protected boolean isAbsoluteExtrusion = false;
    protected File slicerResultWithRelativeE;
    protected RandomAccessFile output;

    protected Slicer(String name, JSONObject slicerConfig) {
        this.name = name;
        this.slicerConfig = slicerConfig;
    }

    public String getName() {
        return name;
    }

    public void setArguments(Arguments args) {
        this.args = args;
    }

    public Arguments getArguments() {
        return args;
    }

    public JSONObject getSlicerConfig() {
        return slicerConfig;
    }

    public void postProcess(File inputGCode, File resultGCode) throws IOException{
        String newLine = System.lineSeparator();

        // isAbsoluteExtrusion = false;
        java.util.regex.Pattern patternAbsolute = java.util.regex.Pattern.compile("^(M82\\s)");
        java.util.regex.Pattern patternExtrusion = java.util.regex.Pattern.compile("(E([\\-0-9\\.]+)\\s?)");

        layerCount = 0;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(;LAYER:)");

        try (Scanner sc = new Scanner(inputGCode, "UTF-8")) {
            while (sc.hasNextLine()) {
                String strLine = sc.nextLine();
                if (pattern.matcher(strLine).find()) {
                    layerCount++;
                    // System.out.println(strLine);
                }
                if (!isAbsoluteExtrusion) {
                    //Matcher mExtrusion = patternExtrusion.matcher(strLine);
                    if (patternAbsolute.matcher(strLine).find()) {
                        isAbsoluteExtrusion = true;
                    }
//                    if (mExtrusion.find()){
//                        double currentExtrusion = Double.parseDouble(mExtrusion.group(2));
//                        // 1 is a huge value for the start of a layer if relative
//                        // this code will find an absolute extrusion if the layers are really tiny maybe at third or forth layer
//                        if (layerCount>0 && currentExtrusion>2){
//                            isAbsoluteExtrusion = true;
//                        }
//                    }
                }
            }
        }

        if (isAbsoluteExtrusion){
            Scanner scExtrusion = new Scanner(inputGCode, "UTF-8");
            slicerResultWithRelativeE = new File(resultGCode.getParentFile(), resultGCode.getName().replace(".gcode", ".slicer.relative.gcode"));
            PrintWriter outputRelative = new PrintWriter(slicerResultWithRelativeE);

            double previousExtrusion = 0.0;
            double currentExtrusion;
            double newExtrusion;
            // DecimalFormat df = new DecimalFormat("#.#####");
            Matcher m;
            while (scExtrusion.hasNextLine()) {
                String strLine = scExtrusion.nextLine();
                m = patternExtrusion.matcher(strLine);
                if (m.find()) {

                    currentExtrusion = Double.parseDouble(m.group(2));
                    if (currentExtrusion>0) {
                        newExtrusion = currentExtrusion - previousExtrusion;
                        // System.out.println("Line : " + strLine + ", E : " + currentExtrusion + ", ENew :" + newExtrusion + ", EReplace : " + m.group(0));
                        // System.out.println("m.group(0) :" + m.group(0));
                        // String newLine = strLine.replace(m.group(0), String.format(Locale.ROOT, "E%.5g%n", newExtrusion));
                        String newLine = strLine.replace(m.group(0), "E"+(double)Math.round(newExtrusion * 100000d) / 100000d);
                        // System.out.println("new Line :" + newLine);
                        outputRelative.write(newLine + newLine);
                    } else {
                        outputRelative.write(strLine + newLine);
                    }
                    previousExtrusion = currentExtrusion;
                } else {
                    outputRelative.write(strLine + newLine);
                }
            }
            // if extrusion was absolute use the relative E converted file as input
            inputGCode = slicerResultWithRelativeE;
        }

        // reset scanner
        try (Scanner sc = new Scanner(inputGCode, "UTF-8");
             RandomAccessFile output = new RandomAccessFile(resultGCode, "rw")) {

            this.output = output;
            output.setLength(0);

            double minExtrusionLength = 0;  // Minimum extrusion length before we will allow a retraction
            double minTravelDistance = 0.01;  // Minimum distance of travel before we actually take the command seriously
            double minPrintDistance = 0.01;  // Minimum distance of printing before we send the command to the postprocessor

            String oldHint = "";
            String hint = "";
            double currentX = 0.00;
            double currentY = 0.00;
            double currentZ = 0.00;
            double lastZ = 0.00;
            double commandDistance = 0.00;
            int currentSpeed = 0;
            int lastMoveWasTravel = 0;
            int lastMoveWasRetract = 0;
            double totalExtrusion=0;
            boolean extruding = false;
            boolean printMoveValid = false;


            String commandX;
            String commandY;
            String commandZ;
            String commandE;
            String commandSpeed;
            String comment;


            while (sc.hasNextLine()) {
                String strLine = sc.nextLine();
                Matcher m;
                if ((m = java.util.regex.Pattern.compile("(X([\\-0-9\\.]+)\\s+Y([\\-0-9\\.]+))").matcher(strLine)).find()) {
                    double newX = Double.parseDouble(m.group(2));
                    double newY = Double.parseDouble(m.group(3));

                    commandDistance = Math.sqrt(Math.pow(newX - currentX, 2) + Math.pow(newY - currentY, 2));
                }
                if ((m = java.util.regex.Pattern.compile("^(M204\\s+S(\\d+))").matcher(strLine)).find()) {
                    // printf NEW "M201 X%d Y%d Z%d E2000\n", $2, $2, $2;
                    writeOutput(String.format("M201 X%d Y%d Z%d E2000" + newLine, m.group(2), m.group(2), m.group(2)));
                } else if (java.util.regex.Pattern.compile("^(M190\\s)").matcher(strLine).find()) {
                    // Remove bed temperature settings
                } else if (java.util.regex.Pattern.compile("^(M104\\s)").matcher(strLine).find()) {
                    // Remove nozzle temperature settings
                } else if (java.util.regex.Pattern.compile("^(M109\\s)").matcher(strLine).find()) {
                    // Remove set temperature and wait
                } else if (java.util.regex.Pattern.compile("^(G21\\s)").matcher(strLine).find()) {
                    // Remove set units to mm
                } else if (java.util.regex.Pattern.compile("^(G90\\s)").matcher(strLine).find()) {
                    // Remove use absolute coordinates
                } else if (java.util.regex.Pattern.compile("^(M83\\s)").matcher(strLine).find()) {
                    // Remove use relative distances for extrusion
                } else if (java.util.regex.Pattern.compile("^(M82\\s)").matcher(strLine).find()) {
                    // Remove use absolute distances for extrusion
                } else if (java.util.regex.Pattern.compile("^(G28\\s)").matcher(strLine).find()) {
                    // Remove home
                } else if (java.util.regex.Pattern.compile("^(G1\\s+)").matcher(strLine).find()) {
                    String outputCommand="";
                    printMoveValid = true;
                    
                    // System.out.println("G1---:"+strLine);
                    // Grab all possible commands
                    m = java.util.regex.Pattern.compile("(X([\\-0-9\\.]+)\\s?)").matcher(strLine);
                    if (m.find()) { commandX = m.group(2); } else { commandX = "false"; }

                    m = java.util.regex.Pattern.compile("(Y([\\-0-9\\.]+)\\s?)").matcher(strLine);
                    if (m.find()) { commandY = m.group(2); } else { commandY = "false"; }

                    m = java.util.regex.Pattern.compile("(Z([\\-0-9\\.]+)\\s?)").matcher(strLine);
                    if (m.find()) { commandZ = m.group(2); } else { commandZ = "false"; }

                    m = java.util.regex.Pattern.compile("(E([\\-0-9\\.]+)\\s?)").matcher(strLine);
                    if (m.find()) { commandE = m.group(2);} else { commandE = "false"; }

                    m = java.util.regex.Pattern.compile("(F([\\-0-9\\.]+)\\s?)").matcher(strLine);
                    if (m.find()) { commandSpeed = m.group(2); } else { commandSpeed = "false"; }

                    m = java.util.regex.Pattern.compile(";\\s+(.+)$").matcher(strLine);
                    if (m.find()) { comment = m.group(1); } else { comment = "false"; }

                    // Output hints
                    if (comment.equals("skirt") || comment.equals("brim")) {
                        hint = "SKIRT";
                    }
                    if (comment.equals("perimeter")) {
                        hint = "WALL-OUTER";
                    }
                    if (comment.equals("infill")) {
                        hint = "SKIN";
                    }
                    if (comment.equals("support material") || comment.equals("support material interface")) {
                        hint = "SUPPORT";
                    }

                    if (hint != oldHint) {
                        writeOutput(";TYPE:" + hint + newLine);
                        oldHint = hint;
                    }

                    // Figure out if this is a travel move or not
                    if (!commandE.equals("false")){
                        outputCommand = "G1";	// Printing move
                    } else {
                        outputCommand = "G0";	// Travel move
                    }

                    // ---- Build command for output ----

                    // Output speed if present
                    if (!commandSpeed.equals("false")){
                        // convert speed to double and round the result ! be cause sometimes the speed is given with a point
                        currentSpeed = (int)Math.round(Double.parseDouble(commandSpeed));
                    }

                    // Remember the Z position if required
                    if (!commandZ.equals("false")){
                        currentZ = Double.parseDouble(commandZ);
                        oldHint = "";
                    }

                    // Output the X and Y position if required
                    if (!commandX.equals("false") && !commandY.equals("false")){

                        outputCommand += String.format(" F%s X%s Y%s", currentSpeed, commandX, commandY);
                        if (lastZ != currentZ){
                            outputCommand += String.format(" Z%s", currentZ);
                        }

                        lastZ = currentZ;
                    }

                    extruding = false;
                    
                    if (!commandE.equals("false")){
                        // Find retract/unretract
                        if (commandX.equals("false") && commandY.equals("false")){
                            // Retract/unretract

                            // Print extrusion if not straight after the first layer change
                            // if (totalExtrusion > 0){
                                // $outputCommand .= sprintf " E%s", $commandE;
                                outputCommand += String.format(" E%s",commandE);
                            // }
                        } else {
                            // Normal print move
                            // $outputCommand .= sprintf " E%s", $commandE;
                            outputCommand += String.format(" E%s",commandE);
                            extruding = true;
                        }
                        totalExtrusion += Double.parseDouble(commandE);

                    }

					// Don't output printing moves with zero movement
					if((extruding == true) && (commandDistance < minPrintDistance)){
						printMoveValid = false;
					}
					
                    // Send the command to the file
                    if ((printMoveValid == true) && (outputCommand.length() > 2)){
                        // printf NEW "%s\n", $outputCommand;
                        // LOGGER.info("output : "+outputCommand);
                        writeOutput(String.format("%s" + newLine,outputCommand));
                    }
                }  else if (java.util.regex.Pattern.compile("^(;LAYER:0)").matcher(strLine).find()) {
                    // Output the layer count
                    writeOutput(String.format(";Layer count: %d" + newLine, layerCount));
                    writeOutput(strLine + newLine);
                    // extrusionAfterRetraction = 0;
                } else if (java.util.regex.Pattern.compile("^(;LAYER:)").matcher(strLine).find()) {
                    // Output the layer number
                    writeOutput(strLine + newLine);
                    // $extrusionAfterRetraction = 0;
                }  else {
                    // System.out.println(strLine);
                    // StandardCharsets.UTF_8.encode(strLine).array()
                    writeOutput(strLine + newLine);
                }

                // Save the current position
                if ((m = Pattern.compile("(X([\\-0-9\\.]+)\\s+Y([\\-0-9\\.]+))").matcher(strLine)).find()) {
                    currentX = Double.parseDouble(m.group(2));
                    currentY = Double.parseDouble(m.group(3));
                }

                if (lastMoveWasTravel > 0) {
                    lastMoveWasTravel--;
                }
                if (lastMoveWasRetract > 0) {
                    lastMoveWasRetract--;
                }
            }
        }


    }
    protected void writeOutput(String textToWrite) throws IOException {
        // LOGGER.info(textToWrite);
        output.write(StandardCharsets.UTF_8.encode(textToWrite).array());
    }

    public abstract void invoke(File resultGCode) throws IOException, InterruptedException;
}
