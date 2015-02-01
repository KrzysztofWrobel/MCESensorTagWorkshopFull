package com.zinno.sensortag;

/**
 * Created by krzysztofwrobel on 28/01/15.
 */
public class MathUtils {

    public static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static double distance(float[] values1, float[] values2) {
        if (values1 != null && values2 != null) {
            if (values1.length == values2.length) {
                float sum = 0;
                for (int i = 0; i < values1.length; i++) {
                    sum += (values2[i] - values1[i]) * (values2[i] - values1[i]);
                }

                return Math.sqrt(sum);
            }
        }

        return -1;
    }

    public static class LowPassFilter {
        private float factor;
        private float[] prevAcc;

        public LowPassFilter() {
            this.factor = 0.01f;
            this.reset();
        }

        public LowPassFilter(float factor) {
            this.factor = factor;
            this.reset();
        }

        public void reset() {
            this.prevAcc = new float[]{0.0f, 0.0f, 0.0f};
        }

        public float[] filterAlgorithm(float[] vector) {
            float[] retVal = new float[3];
            retVal[0] = vector[0] * this.factor + this.prevAcc[0] * (1.0f - this.factor);
            retVal[1] = vector[1] * this.factor + this.prevAcc[1] * (1.0f - this.factor);
            retVal[2] = vector[2] * this.factor + this.prevAcc[2] * (1.0f - this.factor);
            this.prevAcc = retVal;
            return retVal;
        }
    }

}
