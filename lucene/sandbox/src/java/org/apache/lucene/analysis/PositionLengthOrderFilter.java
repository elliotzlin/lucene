package org.apache.lucene.analysis;

import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.util.AttributeSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public final class PositionLengthOrderFilter extends TokenFilter {
  private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);
  private final PositionLengthAttribute posLenAtt = addAttribute(PositionLengthAttribute.class);

  private ArrayList<PositionLengthOrderFilter.AttributeState> cache = null;
  private int cacheIdx = -1;

  // Position increment of current token position being considered.
  private int positionIncrement = -1;

  // Variables to track state from input.
  private AttributeSource.State inputState = null;
  private int inputPositionIncrement = -1;
  private int inputPositionLength = -1;

  public PositionLengthOrderFilter(TokenStream input) {
    super(input);
  }

  @Override
  public void reset() throws IOException {
    inputPositionIncrement = -1;
    inputPositionLength = -1;
    if (cache != null) {
      cache.clear();
      cacheIdx = -1;
    }
    super.reset();
  }

  @Override
  public boolean incrementToken() throws IOException {
    PositionLengthOrderFilter.AttributeState attState;
    if (inputPositionIncrement > 0) {
      // Input indicated incrementing position.
      if (cacheIdx >= 0 && (++cacheIdx < cache.size() || clearCache())) {
        // Emit state from cache if nonempty and non-exhausted.
        attState = cache.get(cacheIdx);
        cacheIdx += 1;
        restoreState(attState.state);
        return true;
      } else {
        // Cache exhausted, consume input position increment.
        int restorePosInc = inputPositionIncrement;
        inputPositionIncrement = -1;
        if (inputPositionLength > 1) {
          // If input position length > 1, store state in cache.
          positionIncrement = restorePosInc;
          storeCache(inputPositionLength, inputState);
        } else if (inputPositionLength == Integer.MIN_VALUE) {
          // Input position length set to min integer, end of stream.
          inputPositionLength = -1;
          restoreState(inputState);
          return false;
        } else {
          // Input position length probably 1, emit token from input.
          restoreState(inputState);
          return true;
        }
      }
    }
    int posLen;
    do {
      restoreState(inputState);
      if (input.incrementToken()) {
        // Store input token's position attributes in local variables.
        int posInc = posIncAtt.getPositionIncrement();
        posLen = posLenAtt.getPositionLength();
        if (posInc > 0) {
          if (cache != null && !cache.isEmpty()) {
            // Incremented token and cache non-empty; get next cached state.
            if (cache.size() == 1) {
              attState = cache.remove(0);
            } else {
              Collections.sort(cache);
              attState = cache.get(cacheIdx = 0);
            }
            // Save input token position metadata, setting position increment attribute to 0 if
            // greater than 1 (we might reorder token).
            inputPositionIncrement = posInc;
            inputPositionLength = posLen;
            if (posLen > 1) {
              posIncAtt.setPositionIncrement(0);
            }
            inputState = captureState();
            // Restore state (setting position increment if necessary), set payload and emit token.
            restoreState(attState.state);
            if (positionIncrement > 0) {
              posIncAtt.setPositionIncrement(positionIncrement);
              positionIncrement = -1;
            }
            return true;
          }
          // Cache is empty; cache token state if position length > 1 (see while loop condition)
          // otherwise emit token.
          positionIncrement = posInc;
          posIncAtt.setPositionIncrement(0);
        }
        // If position increment is 0, store token state in cache if position length greater than
        // 1 (see while condition below). Otherwise emit token.
      } else {
        // No more tokens from input, start loading cached states.
        if (cache != null && !cache.isEmpty()) {
          if (cache.size() == 1) {
            attState = cache.remove(0);
          } else {
            Collections.sort(cache);
            attState = cache.get(cacheIdx = 0);
          }
          // Set input variables to extreme values, not initial values (i.e. -1).
          inputPositionIncrement = Integer.MAX_VALUE;
          inputPositionLength = Integer.MIN_VALUE;
          inputState = captureState();
          restoreState(attState.state);
          if (positionIncrement > 0) {
            posIncAtt.setPositionIncrement(positionIncrement);
            positionIncrement = -1;
          }
          return true;
        }
        // Exhausted all tokens from input and cache; end of stream.
        return false;
      }
    } while (posLen > 1 && storeCache(posLen, captureState()));
    if (positionIncrement > 0) {
      posIncAtt.setPositionIncrement(positionIncrement);
      positionIncrement = -1;
    }
    return true;
  }

  private boolean storeCache(int inputPositionLength, AttributeSource.State state) {
    if (cache == null) {
      cache = new ArrayList<>(4);
    }
    cache.add(new PositionLengthOrderFilter.AttributeState(state, inputPositionLength));
    return true;
  }

  private boolean clearCache() {
    cacheIdx = -1;
    cache.clear();
    return false;
  }

  private static class AttributeState implements Comparable<PositionLengthOrderFilter.AttributeState> {
    private final AttributeSource.State state;
    private final int positionLength;

    public AttributeState(AttributeSource.State state, int positionLength) {
      this.state = state;
      this.positionLength = positionLength;
    }

    @Override
    public int compareTo(PositionLengthOrderFilter.AttributeState o) {
      return Integer.compare(this.positionLength, o.positionLength);
    }
  }
}
