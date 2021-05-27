package org.apache.lucene.analysis;

import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.Arrays;

public class TestPositionLengthOrderFilter extends BaseTokenStreamTestCase {
  public void testOverlappingTokens() throws IOException {
    // There are multiple 'b' tokens on the same position, but one has a greater poslen.
    final Token[] tokens = new Token[5];
    tokens[0] = new Token();
    tokens[0].append("a");
    tokens[0].setPositionIncrement(1);
    tokens[0].setPositionLength(1);
    tokens[1] = new Token();
    tokens[1].append("b");
    tokens[1].setPositionIncrement(1);
    tokens[1].setPositionLength(2);
    tokens[2] = new Token();
    tokens[2].append("b");
    tokens[2].setPositionIncrement(0);
    tokens[2].setPositionLength(1);
    tokens[3] = new Token();
    tokens[3].append("c");
    tokens[3].setPositionIncrement(1);
    tokens[3].setPositionLength(1);
    tokens[4] = new Token();
    tokens[4].append("d");
    tokens[4].setPositionIncrement(1);
    tokens[4].setPositionLength(1);

    PositionLengthOrderFilter tokFilter =
        new PositionLengthOrderFilter(new CannedTokenStream(tokens));

    String[] tokenTypes = new String[5];
    Arrays.fill(tokenTypes, TypeAttribute.DEFAULT_TYPE);

    assertTokenStreamContents(
        tokFilter,
        new String[] {"a", "b", "b", "c", "d"},
        null,
        null,
        tokenTypes,
        new int[] {1, 1, 0, 1, 1},
        new int[] {1, 1, 2, 1, 1});

    // Reverse position length for tokens 'b'.
    tokens[1].setPositionLength(1);
    tokens[2].setPositionLength(2);

    tokFilter = new PositionLengthOrderFilter(new CannedTokenStream(tokens));

    assertTokenStreamContents(
        tokFilter,
        new String[] {"a", "b", "b", "c", "d"},
        null,
        null,
        tokenTypes,
        new int[] {1, 1, 0, 1, 1},
        new int[] {1, 1, 2, 1, 1});
  }
}
