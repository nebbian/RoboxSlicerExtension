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

import static java.util.regex.Pattern.compile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

/**
 * This is base class for various slicers.
 *
 */
public abstract class Slicer {

    // private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

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

        // isAbsoluteExtrusion = false;
        Pattern patternAbsolute = compile("^(M82\\s)");
        Pattern patternExtrusion = compile("(E([\\-0-9\\.]+)\\s?)");

        layerCount = 0;
        Pattern pattern = compile("^(;LAYER:)");

        try (Scanner sc = new Scanner(inputGCode, "UTF-8")) {
            while (sc.hasNextLine()) {
                String strLine = sc.nextLine();
                if (pattern.matcher(strLine).find()) {
                    layerCount++;
                    // System.out.println(strLine);
                }
                if (!isAbsoluteExtrusion) {
                    // Matcher mExtrusion = patternExtrusion.matcher(strLine);
                    if (patternAbsolute.matcher(strLine).find()) {
                        isAbsoluteExtrusion = true;
                    }
//                    if (mExtrusion.find()) {
//                        double currentExtrusion = Double.parseDouble(mExtrusion.group(2));
//                        // 1 is a huge value for the start of a layer if relative
//                        // this code will find an absolute extrusion if the layers are really tiny maybe at third or forth layer
//                        if (layerCount>0 && currentExtrusion>2) {
//                            isAbsoluteExtrusion = true;
//                        }
//                    }
                }
            }
        }

        if (isAbsoluteExtrusion) {
            try (Scanner scExtrusion = new Scanner(inputGCode, "UTF-8")) {
                slicerResultWithRelativeE = new File(resultGCode.getParentFile(), resultGCode.getName().replace(".gcode", ".slicer.relative.gcode"));
                try (PrintWriter outputRelative = new PrintWriter(slicerResultWithRelativeE)) {

                    DecimalFormat extrusionFormat = new DecimalFormat("###0.00000");

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
                                String newLine = strLine.replace(m.group(0), "E"+extrusionFormat.format(Math.round(newExtrusion * 100000d) / 100000d));
                                // System.out.println("new Line :" + newLine);
                                outputRelative.println(newLine);
                            } else {
                                outputRelative.println(strLine);
                            }
                            previousExtrusion = currentExtrusion;
                        } else {
                            outputRelative.println(strLine);
                        }
                    }
                    // if extrusion was absolute use the relative E converted file as input
                    inputGCode = slicerResultWithRelativeE;
                }
            }
        }

        // reset scanner
        try (Scanner sc = new Scanner(inputGCode, "UTF-8");
             RandomAccessFile output = new RandomAccessFile(resultGCode, "rw")) {

            this.output = output;
            output.setLength(0);

            // double minExtrusionLength = 0;  // Minimum extrusion length before we will allow a retraction
            // double minTravelDistance = 0.01;  // Minimum distance of travel before we actually take the command seriously
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

            @SuppressWarnings("unused") // TODO do we need it or we can remove it?
            double totalExtrusion = 0;

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
                if ((m = compile("(X([\\-0-9\\.]+)\\s+Y([\\-0-9\\.]+))").matcher(strLine)).find()) {
                    double newX = Double.parseDouble(m.group(2));
                    double newY = Double.parseDouble(m.group(3));

                    commandDistance = Math.sqrt(Math.pow(newX - currentX, 2) + Math.pow(newY - currentY, 2));
                }
                if ((m = compile("^(M204\\s+S(\\d+))").matcher(strLine)).find()) {
                    // printf NEW "M201 X%d Y%d Z%d E2000\n", $2, $2, $2;
                    writeOutputLine(String.format("M201 X%d Y%d Z%d E2000", m.group(2), m.group(2), m.group(2)));
                } else if (compile("^(M190\\s)").matcher(strLine).find()) {
                    // Remove bed temperature settings
                } else if (compile("^(M104\\s)").matcher(strLine).find()) {
                    // Remove nozzle temperature settings
                } else if (compile("^(M109\\s)").matcher(strLine).find()) {
                    // Remove set temperature and wait
                } else if (compile("^(G21\\s)").matcher(strLine).find()) {
                    // Remove set units to mm
                } else if (compile("^(G90\\s)").matcher(strLine).find()) {
                    // Remove use absolute coordinates
                } else if (compile("^(M83\\s)").matcher(strLine).find()) {
                    // Remove use relative distances for extrusion
                } else if (compile("^(M82\\s)").matcher(strLine).find()) {
                    // Remove use absolute distances for extrusion
                } else if (compile("^(G28\\s)").matcher(strLine).find()) {
                    // Remove home
                } else if ((compile("^(G1\\s+)").matcher(strLine).find()) ||
                             (compile("^(G0\\s+)").matcher(strLine).find())) {
                    String outputCommand="";
                    printMoveValid = true;

                    // System.out.println("G1---:"+strLine);
                    // Grab all possible commands
                    m = compile("(X([\\-0-9\\.]+)\\s?)").matcher(strLine);
                    if (m.find()) { commandX = m.group(2); } else { commandX = "false"; }

                    m = compile("(Y([\\-0-9\\.]+)\\s?)").matcher(strLine);
                    if (m.find()) { commandY = m.group(2); } else { commandY = "false"; }

                    m = compile("(Z([\\-0-9\\.]+)\\s?)").matcher(strLine);
                    if (m.find()) { commandZ = m.group(2); } else { commandZ = "false"; }

                    m = compile("(E([\\-0-9\\.]+)\\s?)").matcher(strLine);
                    if (m.find()) { commandE = m.group(2);} else { commandE = "false"; }

                    m = compile("(F([\\-0-9\\.]+)\\s?)").matcher(strLine);
                    if (m.find()) { commandSpeed = m.group(2); } else { commandSpeed = "false"; }

                    m = compile(";\\s+(.+)$").matcher(strLine);
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
                        writeOutputLine(";TYPE:" + hint);
                        oldHint = hint;
                    }

                    // Figure out if this is a travel move or not
                    if (!commandE.equals("false")) {
                        outputCommand = "G1";    // Printing move
                    } else {
                        outputCommand = "G0";    // Travel move
                    }

                    // ---- Build command for output ----

                    // Output speed if present
                    if (!commandSpeed.equals("false")) {
                        // convert speed to double and round the result ! be cause sometimes the speed is given with a point
                        currentSpeed = (int)Math.round(Double.parseDouble(commandSpeed));
                    }

                    // Remember the Z position if required
                    if (!commandZ.equals("false")) {
                        currentZ = Double.parseDouble(commandZ);
                        oldHint = "";
                    }

                    // Output the X and Y position if required
                    if (!commandX.equals("false") && !commandY.equals("false")) {

                        outputCommand += String.format(" F%s X%s Y%s", currentSpeed, commandX, commandY);
                        if (lastZ != currentZ) {
                            outputCommand += String.format(" Z%s", currentZ);
                        }

                        lastZ = currentZ;
                    }

                    extruding = false;

                    if (!commandE.equals("false")) {
                        // Find retract/unretract
                        if (commandX.equals("false") && commandY.equals("false")) {
                            // Retract/unretract

                            // Print extrusion if not straight after the first layer change
                            // if (totalExtrusion > 0) {
                                // $outputCommand .= sprintf " E%s", $commandE;
                                outputCommand += String.format(" E%s", commandE);
                            // }
                        } else {
                            // Normal print move
                            // $outputCommand .= sprintf " E%s", $commandE;
                            outputCommand += String.format(" E%s", commandE);
                            extruding = true;
                        }
                        totalExtrusion += Double.parseDouble(commandE);
                    }

                    // Don't output printing moves with zero movement
                    if ((extruding == true) && (commandDistance < minPrintDistance)) {
                        printMoveValid = false;
                    }

                    // Send the command to the file
                    if ((printMoveValid == true) && (outputCommand.length() > 2)) {
                        // printf NEW "%s\n", $outputCommand;
                        // LOGGER.info("output : "+outputCommand);
                        writeOutputLine(String.format("%s", outputCommand));
                    }
                }  else if (compile("^(;LAYER:0)").matcher(strLine).find()) {
                    // Output the layer count
                    writeOutputLine(String.format(";Layer count: %d", layerCount));
                    writeOutputLine(strLine);
                    // extrusionAfterRetraction = 0;
                } else if (compile("^(;LAYER:)").matcher(strLine).find()) {
                    // Output the layer number
                    writeOutputLine(strLine);
                    // $extrusionAfterRetraction = 0;
                } else if ((m = compile("^(T(\\d+))").matcher(strLine)).find()) {
                    // Change extruder
                    writeOutputLine(String.format("T%s", m.group(2)));
                }  else {
                    // System.out.println(strLine);
                    // StandardCharsets.UTF_8.encode(strLine).array()
                    writeOutputLine(strLine);
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

    protected void writeOutputLine(String textToWrite) throws IOException {
        writeOutput(textToWrite + System.lineSeparator());
    }

    public abstract void invoke(File resultGCode) throws IOException, InterruptedException;

    public File chooseOneFile(List<File> foundFiles) {
        // By default we just pick first!
        // TODO is it right?
        return foundFiles.get(0);
    }
}
