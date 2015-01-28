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
}
