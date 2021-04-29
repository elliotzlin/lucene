package org.apache.lucene.analysis;

import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * Stores a token's position length as a payload.
 */
public class PositionLengthPayloadTokenFilter extends TokenFilter {
  PositionLengthAttribute posLenAtt = addAttribute(PositionLengthAttribute.class);
  PayloadAttribute payAtt = addAttribute(PayloadAttribute.class);

  public PositionLengthPayloadTokenFilter(TokenStream input) {
    super(input);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      payAtt.setPayload(encodeInt(posLenAtt.getPositionLength()));
      return true;
    }
    return false;
  }

  private BytesRef encodeInt(int i) {
    byte[] data = new byte[] {
        (byte) (i >>> 24),
        (byte) (i >>> 16),
        (byte) (i >>> 8),
        (byte) i,
    };
    return new BytesRef(data);
  }
}