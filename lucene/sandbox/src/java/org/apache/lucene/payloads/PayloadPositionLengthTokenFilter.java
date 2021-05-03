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
public final class PayloadPositionLengthTokenFilter extends TokenFilter {
  PositionLengthAttribute posLenAtt = addAttribute(PositionLengthAttribute.class);
  PayloadAttribute payAtt = addAttribute(PayloadAttribute.class);

  public PayloadPositionLengthTokenFilter(TokenStream input) {
    super(input);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      payAtt.setPayload(encodePosLen(posLenAtt.getPositionLength()));
      return true;
    }
    return false;
  }

  public static BytesRef encodePosLen(int i) {
    byte[] data = new byte[] {
        (byte) (i >>> 24),
        (byte) (i >>> 16),
        (byte) (i >>> 8),
        (byte) i,
    };
    return new BytesRef(data);
  }

  public static int decodePosLen(BytesRef payload) {
    if (payload == null) {
      return 1;
    }
    int posLen = 0;
    for (int i = 0; i < payload.length; i++) {
      posLen = (posLen << 8) | Byte.toUnsignedInt(payload.bytes[i]);
    }
    return posLen;
  }
}
