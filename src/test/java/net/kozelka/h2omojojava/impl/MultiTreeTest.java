package net.kozelka.h2omojojava.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

@RunWith(Parameterized.class)
public class MultiTreeTest {
    private static final File trees = new File("src/test/resources/gbm_v1.00_names.mojo/trees/");
    //    private static final File trees = new File("src/test/resources/gbm_v1.00_titanic.mojo/trees/");
//    private static final File trees = new File("/home/pk/h2o/h2o-mojo/testmodels/prostate/unzipped/trees");
//    private static final File trees = new File("/home/pk/h2o/h2o-mojo/testmodels/airlines1/unzipped/trees");
//    private static final File trees = new File("/home/pk/h2o/h2o-mojo/testmodels/airlines2/unzipped/trees");

    // v1.30 fails:
//    private static final File trees = new File("/home/pk/Downloads/Travelport-OneDrive_2018-09-26/SU/model/trees");

    @Parameterized.Parameters(name= "{0}")
    public static Iterable<? extends String> data() {
        final String[] filenames = trees.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith("_aux.bin")) return false;
                return name.endsWith(".bin");
            }
        });
        Arrays.sort(filenames);
        return Arrays.asList(filenames);
//        return Arrays.stream(filenames).limit(15).collect(Collectors.toList());
    }

    @Parameterized.Parameter
    public String filename;

    @Test
    public void test() throws IOException {
        final MojoTreeReader mtr = new MojoTreeReader(new File(trees, filename), 100 /*130*/);
        final MtrNode root = mtr.readRootNode();

    }
}
