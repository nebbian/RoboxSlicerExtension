package com.roboxing.slicerextension.flow;

import java.io.File;
import java.io.IOException;

public class DefaultAMCura extends Slicer {

    public DefaultAMCura() {
        super("DefaultAMCura");
    }

    @Override
    public void postProcess(File in, File out) throws IOException {
        if (!in.renameTo(out)) {
            throw new IOException("Cannot rename '" + in.getPath() + "' to '" + out.getPath() + "'");
        }
    }

    @Override
    public void invoke() throws IOException {

    }

}
