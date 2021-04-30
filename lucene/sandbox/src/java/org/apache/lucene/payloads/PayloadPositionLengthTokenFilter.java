package org.apache.lucene.payloads;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * Stores a token's position length as a payload.
 */
public class PayloadPositionLengthTokenFilter extends TokenFilter {
  PositionLengthAttribute posLenAtt = addAttribute(PositionLengthAttribute.class);
  PayloadAttribute payAtt = addAttribute(PayloadAttribute.class);

  public PayloadPositionLengthTokenFilter(TokenStream input) {
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