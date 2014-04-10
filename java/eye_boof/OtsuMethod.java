package eye_boof;

import boofcv.struct.image.ImageUInt8;

public final class OtsuMethod {

    public static int computeThreshold (int[] hist) {
        int histSum = 0;
        int histIdxSum = 0; // sum(hist[i] * i)
        for (int i = 0; i < hist.length; i++) {
            histSum += hist[i];
            histIdxSum += i * hist[i];
        }

        /* The probality of the two classes. */
        int w1 = 0;
        int w2 = 0;
        int w1Idx = 0; // w1 * i
        /* Maximum value of the inter-class variance. */
        double maxVar = 0.0;
        int maxVarIdx = -1;
        for (int i = 0; i < hist.length; i++) {
            w1 += hist[i];
            w2 = histSum - w1;
            /* When all nonzeros of the histogram array were already
            visited. */
            if (w2 == 0)
                break;
            /* The first nonzero value was not reached yet. */
            if (w1 == 0)
                continue;
            w1Idx += hist[i] * i;
            double mu1 = w1Idx / w1;
            double mu2 = (histIdxSum - w1Idx) / w2;
            double var = Math.pow(mu1 - mu2, 2) * w1 * w2;
            if (var > maxVar) {
                maxVar = var;
                maxVarIdx = i;
            }

        }
        return maxVarIdx;
    }
}
