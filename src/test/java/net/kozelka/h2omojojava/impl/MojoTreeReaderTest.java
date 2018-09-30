package net.kozelka.h2omojojava.impl;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MojoTreeReaderTest {

    /*
0000:
  0c 00 00 04 04 00 2b 00  5d 05 35 0e 12 5c 6c ba
0010:
  c2 d7 40 22 84 d0 4b 69  35 dc 87 0e 58 1f 82 6c
0020:
  9f 40 10 10 80 b1 4b 20  d2 32 a7 86 62 d4 e8 a8
0030:
  64 ff 0a 53 3c 00 00 04  04 00 2b 00 5d 05 35 0e
0040:
  12 5c 6c ba c2 d7 40 22  84 d0 4b 69 35 dc 87 0e
0050:
  78 1f 82 6c 9f 40 10 10  90 b1 4b 20 d2 32 a7 86
0060:
  62 d4 e8 a8 64 ff 0a a7  e1 18 be 30 01 00 04 00
0070:
  50 ef 44 a7 e1 18 be f0  02 00 05 00 d8 17 45 ba
0080:
  0e 9b 3e a7 e1 18 be cc  00 00 05 04 00 2b 00 5d
0090:
  05 35 0e 12 5c 6c ba c2  d7 40 22 84 d0 4b 69 35
00a0:
  dc 87 0e 58 1f 82 6c 9f  40 10 10 80 b1 4b 20 d2
00b0:
  32 a7 86 62 d4 e8 a8 64  bf 0a 28 30 01 00 05 00
00c0:
  d0 ed 44 09 84 59 3e 30  02 00 05 00 80 d8 44 eb
00d0:
  20 71 bd f0 01 00 05 00  10 ef 44 09 84 59 3e 00
00e0:
  bc 7c 3d ba 0e 9b 3e
    
     */
    @Test
    public void readSimple() throws IOException {
        final MojoTreeReader mtr = new MojoTreeReader(new File("src/test/resources/gbm_v1.00_names.mojo/trees/t00_000.bin"), 2);
        final List<MtrNode> nodes = mtr.process();
        for (MtrNode node : nodes) {
            System.out.println(node);
        }
    }

    @Test
    public void readRecursive() throws IOException {
        final MojoTreeReader mtr = new MojoTreeReader(new File("src/test/resources/gbm_v1.00_names.mojo/trees/t00_055.bin"), 2);
        final MtrNode root = mtr.readRootNode();
        System.out.println("root = " + root);
    }
}
