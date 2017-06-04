package com.roboxing.slicerextension.flow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Slic3r extends Slicer {

    public Slic3r() {
        super("Slic3r");
    }

    @Override
    public void invoke(File resultGCode) throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add("/Applications/Slic3r.app/Contents/MacOS/Slic3r");
        args.add("--gui");
        args.add("-o");
        args.add(resultGCode.getAbsolutePath());
        for (File f : getArguments().getInptFiles()) {
            args.add(f.getAbsolutePath());
        }

        Process process = new ProcessBuilder(args.toArray(new String[args.size()])).start();

        process.waitFor();
    }

    public void postProcess(File inFile, File outFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outFile);
                FileInputStream fis = new FileInputStream(inFile)) {

            try (PrintWriter out = new PrintWriter(fos);
                    BufferedReader in = new BufferedReader(new InputStreamReader(fis))) {

                String line = in.readLine();
                while (line != null) {
                    out.print(line);

                    // Add line processing in here!

                    line = in.readLine();
                }
            }
        }
    }
}
