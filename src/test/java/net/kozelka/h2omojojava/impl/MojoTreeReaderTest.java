package net.kozelka.h2omojojava.impl;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MojoTreeReaderTest {

    @Test
    public void readRecursive() throws IOException {
        final MojoTreeReader mtr = new MojoTreeReader(new File("src/test/resources/gbm_v1.00_names.mojo/trees/t00_034.bin"), 2, 100);
        final MtrNode root = mtr.readRootNode();
        System.out.println("root = " + root);
    }

    @Test
    public void readV130() throws IOException {
        final MojoTreeReader mtr = new MojoTreeReader(new File("/home/pk/Downloads/Travelport-OneDrive_2018-09-26/SU/model/trees/t00_000.bin"), 2, 130);
        final MtrNode root = mtr.readRootNode();
        System.out.println("root = " + root);
    }
}
