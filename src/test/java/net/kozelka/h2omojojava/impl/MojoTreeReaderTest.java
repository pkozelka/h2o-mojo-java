package net.kozelka.h2omojojava.impl;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MojoTreeReaderTest {

    /*
0000:
  00 05 00 05 3d 22 50 41  9a 00 03 00 03 0e 08 92
0010:
  41 42 00 03 00 05 2f 51  28 41 10 f0 05 00 04 a8
0020:
  b6 43 41 cd cc cc bd 0a  d7 a3 bc 00 04 00 04 00
0030:
  00 00 3f 10 f0 05 00 04  30 a6 14 41 33 33 33 3f
0040:
  83 2d 58 3e f8 02 00 05  02 00 00 00 cd cc cc bd
0050:
  f5 49 9f 3d 00 04 00 04  00 00 00 3f 29 08 02 00
0060:
  05 02 00 00 00 10 f0 05  00 04 00 40 2c 41 52 b8
0070:
  1e 3f 66 66 66 3e f0 03  00 02 00 c8 19 42 33 33
0080:
  33 3f b1 8f 0b 3f c8 02  00 04 02 00 00 00 10 f0
0090:
  05 00 04 66 f6 22 41 4b  ad 54 3e cd cc cc bd 4b
00a0:
  9c fa 3e cc 06 00 02 00  00 18 00 6f 2f ba 56 5a
00b0:
  17 e2 67 d3 7a 9a bb 43  7e af 3e f7 45 a7 e7 ab
00c0:
  23 00 02 4a 00 05 00 05  00 84 8a 41 1c 30 03 00
00d0:
  03 c8 9a 3c 41 cd cc cc  bd f8 00 00 04 01 00 00
00e0:
  00 37 87 12 3e 00 00 00  00 08 00 00 04 03 00 00
00f0:
  00 10 f0 05 00 05 ef 10  c9 41 d6 03 81 bd d1 e1
0100:
  c9 bd f0 03 00 01 be c1  64 bd 27 70 02 3f 33 33
0110:
  33 3f
    
     */
    @Test
    public void name() throws IOException {
        final MojoTreeReader mtr = new MojoTreeReader(new File("src/test/resources/gbm_v1.00_titanic.mojo/trees/t00_000.bin"), 8);
        final List<MtrNode> nodes = mtr.process();
        for (MtrNode node : nodes) {
            System.out.println(node);
        }
    }
}
