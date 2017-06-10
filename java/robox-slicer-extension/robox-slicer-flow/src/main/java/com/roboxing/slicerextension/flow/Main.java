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
 * along with Robox Slicer Extension.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package com.roboxing.slicerextension.flow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class of robox slicer extension
 *
 */
public class Main {
    public static void main(String[] args) {

        System.out.println(Main.class.getClassLoader().getResource("logging.properties"));

        Arguments arguments = new Arguments();
        arguments.process(args);

//        Controller controller = new Controller(arguments);
//        try {
//            controller.process();
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(1);
//        }

        String userCelRoboxPath = System.getProperty("user.home")+"/CEL Robox/";

        File input = null;
        File output = null;

        if(arguments.getOutputFile()!=null) {
            input = arguments.getOutputFile();
            output = new File(arguments.getOutputFile().getParentFile(), arguments.getOutputFile().getName() + ".orig");
        }else{
            //debug mode
            File dir = new File(userCelRoboxPath+"PrintJobs");

            //set printJob as currentDir
            System.setProperty("user.dir", dir.getAbsolutePath());
            //look inside first folder
            for (File folder : dir.listFiles()) {
                if(!folder.isDirectory())
                    continue;

                //rename orig if there is already one
                for (File file : folder.listFiles()) {
                    System.out.println("File : " + file.getName());
                    if (file.getName().endsWith(".orig")) {
                        File gcodeFIle = new File(file.getAbsolutePath().replace(".orig", ""));
                        gcodeFIle.delete();
                        file.renameTo(new File(file.getAbsolutePath().replace(".orig","")));
                        break;
                    }
                }
                for (File file : folder.listFiles()) {
                    System.out.println("File : " + file.getName());
                    if (file.getName().endsWith(".gcode") && !file.getName().endsWith("_robox.gcode")) {
                        //textFiles.add(file.getName());
                        input = file;
                        output = new File(file.getAbsolutePath()+".orig");
                        break;
                    }
                }
                //stop at first valid folder
                break;
            }

        }

        Path currentDir = Paths.get(".").toAbsolutePath().normalize();

        Slicer slicer;
        if (currentDir.toString().contains("PrintJobs")) {
            slicer = new Slic3r();
        } else {
            slicer = new DefaultAMCura();
        }
        slicer.setArguments(arguments);
        try {
            slicer.invoke(input);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if(input == null || output == null) {
                System.out.println("no valid source files");
                return;
            }
            slicer.postProcess(input, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
