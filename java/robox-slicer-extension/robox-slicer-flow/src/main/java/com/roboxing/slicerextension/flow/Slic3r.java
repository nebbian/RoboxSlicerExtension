package com.roboxing.slicerextension.flow;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.nio.charset.StandardCharsets;

public class Slic3r extends Slicer {

    private RandomAccessFile output;

    public Slic3r() {
        super("Slic3r");
    }

    public void postProcess(File in, File out) throws IOException {
        //File input = new File(gcodeFileToProcess);
        String tmpFileName = in.getAbsolutePath() + ".tmp";
        output = new RandomAccessFile(tmpFileName, "rw");
        output.setLength(0);
        Scanner sc = new Scanner(in, "UTF-8");
        //PrintWriter printer = new PrintWriter(output);

        int layerCount = 0;

        while (sc.hasNextLine()) {
            String strLine = sc.nextLine();
            //Matcher m = Pattern.compile("^(;LAYER:)").matcher(strLine);
            if (Pattern.compile("^(;LAYER:)").matcher(strLine).find()) {
                layerCount++;
                System.out.println(strLine);
            }
        }
        System.out.println("total layers : " + layerCount);
        //reset scanner
        sc = new Scanner(in);

        double minExtrusionLength = 0.3;        // Minimum extrusion length before we will allow a retraction
        double minTravelDistance = 0.01;  // Minimum distance of travel before we actually take the command seriously

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


        while (sc.hasNextLine()) {
            String strLine = sc.nextLine();
            Matcher m;
            if ((m = Pattern.compile("(X([\\-0-9\\.]+)\\s+Y([\\-0-9\\.]+))").matcher(strLine)).find()) {
                double newX = Double.parseDouble(m.group(2));
                double newY = Double.parseDouble(m.group(3));

                commandDistance = Math.sqrt(Math.pow(newX - currentX, 2) + Math.pow(newY - currentY, 2));
            }
            if ((m = Pattern.compile("^(M204\\s+S(\\d+))").matcher(strLine)).find()) {
                //printf NEW "M201 X%d Y%d Z%d E2000\n", $2, $2, $2;
                writeOutput(String.format("M201 X%d Y%d Z%d E2000\n", m.group(2), m.group(2), m.group(2)));
            } else if (Pattern.compile("^(M190\\s)").matcher(strLine).find()) {
                // Remove bed temperature settings
            } else if (Pattern.compile("^(M104\\s)").matcher(strLine).find()) {
                // Remove nozzle temperature settings
            } else if (Pattern.compile("^(M109\\s)").matcher(strLine).find()) {
                // Remove set temperature and wait
            } else if (Pattern.compile("^(G21\\s)").matcher(strLine).find()) {
                // Remove set units to mm
            } else if (Pattern.compile("^(G90\\s)").matcher(strLine).find()) {
                // Remove use absolute coordinates
            } else if (Pattern.compile("^(M83\\s)").matcher(strLine).find()) {
                // Remove use relative distances for extrusion
            } else if (Pattern.compile("^(DISABLED_G1\\s+)").matcher(strLine).find()) {
                // Grab all possible commands
                if ((m = Pattern.compile("(X([\\-0-9\\.]+)\\s)").matcher(strLine)).find()) {
                    commandX = m.group(2);
                } else {
                    commandX = "false";
                }
                if ((m = Pattern.compile("(Y([\\-0-9\\.]+)\\s)").matcher(strLine)).find()) {
                    commandY = m.group(2);
                } else {
                    commandY = "false";
                }
                if ((m = Pattern.compile("(Z([\\-0-9\\.]+)\\s)").matcher(strLine)).find()) {
                    commandZ = m.group(2);
                } else {
                    commandZ = "false";
                }
                if ((m = Pattern.compile("(E([\\-0-9\\.]+)\\s)").matcher(strLine)).find()) {
                    commandE = m.group(2);
                } else {
                    commandE = "false";
                }
                if ((m = Pattern.compile("(F([\\-0-9\\.]+)\\s)").matcher(strLine)).find()) {
                    commandSpeed = m.group(2);
                } else {
                    commandSpeed = "false";
                }
                if ((m = Pattern.compile(";\\s+(.+)$").matcher(strLine)).find()) {
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
                    break;    // Break out of the if statement
                }

                if (commandSpeed != "false") {
                }
            } else if ((m = Pattern.compile("^(G1\\s+Z([0-9\\.]+)\\s+)").matcher(strLine)).find()) {
                // Layer change code
                currentZ = Double.parseDouble(m.group(2));
                if (retracted) {
                    writeOutput(String.format("G0 X%s Y%s Z%s \n", currentX, currentY, currentZ));
                } else {
                    outputZ = true;
                    layerChange = true;
                }
                oldHint = "";
            } else if (Pattern.compile("^(;LAYER:0)").matcher(strLine).find()) {
                // Output the layer count
                writeOutput(String.format(";Layer count: %d\n", layerCount));
                writeOutput(strLine + "\n");
                extrusionAfterRetraction = 0;
            } else if (Pattern.compile("^(;LAYER:)").matcher(strLine).find()) {
                // Output the layer number
                writeOutput(strLine + "\n");
                //$extrusionAfterRetraction = 0;
            } else if ((m = Pattern.compile("^(G1\\s+E([\\-0-9\\.]+)\\s+F([0-9]+))").matcher(strLine)).find()) {
                //retraction/unretraction

                double extrusion = Double.parseDouble(m.group(2));
                int feedRate = Integer.parseInt(m.group(3));
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

            } else if ((m = Pattern.compile("^(G1\\s+F([\\-0-9]+))").matcher(strLine)).find()) {
                currentSpeed = Integer.parseInt(m.group(2));
            } else if ((m = Pattern.compile("^(G1\\s+X([\\-0-9\\.]+)\\s+Y([\\-0-9\\.]+)\\sF([0-9]+))").matcher(strLine)).find()) {
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
                            writeOutput(String.format("G0 F%s X%s Y%s Z%s\n", m.group(4), m.group(2), m.group(3), currentZ));
                            outputZ = false;
                            extrusionAfterRetraction = 0;
                        } else {
                            writeOutput(String.format("G0 F%s X%s Y%s E0.00\n", m.group(4), m.group(2), m.group(3)));
                            lastMoveWasTravel = 2;
                        }
                    } else {
                        if (outputZ) {
                            writeOutput(String.format("G0 F%s X%s Y%s Z%s\n", m.group(4), m.group(2), m.group(3), currentZ));
                            outputZ = false;
                        } else {
                            writeOutput(String.format("G0 F%s X%s Y%s\n", m.group(4), m.group(2), m.group(3)));
                        }
                        lastMoveWasTravel = 2;
                    }
                }
            } else if ((m = Pattern.compile("^(G1\\s+X([\\-0-9\\.]+)\\s+Y([\\-0-9\\.]+)\\s+E([\\-0-9\\.]+)\\s+;\\s+(.+))$").matcher(strLine)).find()) {
                // Output hints as to what is going on
                String currentHint = m.group(5);
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

                if ((Double.parseDouble(m.group(2)) == currentX) && (Double.parseDouble(m.group(3)) == currentY)) {
                    // Don't output zero distance moves
                } else {

                    if (currentSpeed == 0) {
                        writeOutput(String.format("G1 X%s Y%s E%s \n", m.group(2), m.group(3), m.group(4)));
                        //printf NEW "G1 X%s Y%s E%s \n", $2, $3, $4;
                    } else {
                        writeOutput(String.format("G1 F%s X%s Y%s E%s \n", currentSpeed, m.group(2), m.group(3), m.group(4)));
                        currentSpeed = 0;
                    }
                    extrusionAfterRetraction += Double.parseDouble(m.group(4));
                }

            } else {
                System.out.println(strLine);
                //StandardCharsets.UTF_8.encode(strLine).array()
                writeOutput(strLine + "\n");
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

        if (!in.renameTo(out)) {
            throw new IOException("Cannot rename '" + in.getPath() + "' to '" + out.getPath() + "'");
        }
        output.close();
        File tmpOutput = new File(tmpFileName);
        if (!tmpOutput.renameTo(in)) {
            throw new IOException("Cannot rename '" + tmpOutput.getPath() + "' to '" + in.getPath() + "'");
        }

    }

    @Override
    public void invoke() throws IOException {

    }

    private void writeOutput(String textToWrite) throws IOException {
        output.write(StandardCharsets.UTF_8.encode(textToWrite).array());
    }

}

