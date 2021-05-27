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
public final class PayloadPositionLengthFilter extends TokenFilter {
  private final PositionLengthAttribute posLenAtt = addAttribute(PositionLengthAttribute.class);
  private final PayloadAttribute payAtt = addAttribute(PayloadAttribute.class);

  public PayloadPositionLengthFilter(TokenStream input) {
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
    int numBitsRequired = 32 - Integer.numberOfLeadingZeros(i);
    int numBytesRequired = (numBitsRequired + 7) / 8;
    BytesRef ret = new BytesRef(4);
    ret.length = numBytesRequired;
    for (int index = numBytesRequired - 1; index >= 0; index--) {
      ret.bytes[index] = (byte) i;
      i >>>= 8;
    }
    assert i == 0;
    return ret;
  }

  public static int decodePosLen(BytesRef payload) {
    if (payload == null) {
      return 1;
    }
    int posLen = 0;
    for (int i = payload.offset; i < payload.offset + payload.length; i++) {
      posLen = (posLen << 8) | Byte.toUnsignedInt(payload.bytes[i]);
    }
    return posLen;
  }
}
