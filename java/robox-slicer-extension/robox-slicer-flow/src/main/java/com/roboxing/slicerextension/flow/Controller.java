package com.roboxing.slicerextension.flow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Controller {

    private Arguments arguments;

    public Controller(Arguments arguments) {
        this.arguments = arguments;
    }

    public void process() throws IOException, InterruptedException {

        Path currentDir = Paths.get(".").toAbsolutePath().normalize();

        Slicer slicer;
        if (currentDir.toString().contains("PrintJobs")) {
            slicer = new Slic3r();
        } else {
            slicer = new DefaultAMCura();
        }

        slicer.setArguments(arguments);

        File slicerResultingGCode = new File(arguments.getOutputFile().getParentFile(), arguments.getOutputFile().getName() + ".slicer");

        slicer.invoke(slicerResultingGCode);

        slicer.postProcess(slicerResultingGCode, arguments.getOutputFile());
    }
}
