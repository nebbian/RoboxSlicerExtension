package com.roboxing.slicerextension.flow;

import java.io.File;
import java.io.IOException;

public class DefaultAMCura extends Slicer {

    public DefaultAMCura() {
        super("DefaultAMCura");
    }

    @Override
    public void postProcess(File slicerGCode, File resultGCode) throws IOException {
        if (!slicerGCode.renameTo(resultGCode)) {
            throw new IOException("Cannot rename '" + slicerGCode.getPath() + "' to '" + resultGCode.getPath() + "'");
        }
    }

    @Override
    public void invoke(File resultGCode) throws IOException, InterruptedException {
        OS os = OS.detect();

        String[] args = getArguments().getOriginalArguments();
        String[] commandAndArgs = new String[args.length + 1];
        commandAndArgs[0] = os.getCuraEngineOrigPath();
        System.arraycopy(args, 0, commandAndArgs, 1, args.length);

        Process process = new ProcessBuilder(commandAndArgs).start();

        process.waitFor();
    }
}
