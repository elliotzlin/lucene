package org.apache.lucene.search;

import org.apache.lucene.util.PriorityQueue;

final class PayloadPhraseQueue extends PriorityQueue<PayloadPhrasePositions> {
  PayloadPhraseQueue(int size) {
    super(size);
  }

  @Override
  protected final boolean lessThan(PayloadPhrasePositions pp1, PayloadPhrasePositions pp2) {
    if (pp1.position == pp2.position)
      // same doc and pp.position, so decide by actual term positions.
      // rely on: pp.position == tp.position - offset.
      if (pp1.offset == pp2.offset) {
        return pp1.ord < pp2.ord;
      } else {
        return pp1.offset < pp2.offset;
      }
    else {
      return pp1.position < pp2.position;
    }
  }
}

