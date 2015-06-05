/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

/**
 *
 * @author Sk8er
 */
public class fuzzifier {

    //double[][] bitmapFunc1, bitmapFunc2, bitmapFunc3;

    //public fuzzifier() {
    //}

    public static double getFuzzyValue(double val, FeatureGeometryType t, double start, double end) {
        double FuzzyValue = 0;
        double length = end - start;
        switch (t) {
            case LINE: {
                double a = start + 0.1 * length;
                double b = end - 0.2 * length;
                if (val < a) {
                    FuzzyValue = 0;
                } else if (val > b) {
                    FuzzyValue = 1;
                } else {
                    FuzzyValue = (val - a) / (b - a);
                }
                break;
            }
            case POLYGON: {
                double a = start + 0.25 * length;
                double b = end - 0.25 * length;
                if (val < a) {
                    FuzzyValue = 0;
                } else if (val > b) {
                    FuzzyValue = 1;
                } else {
                    FuzzyValue = (val - a) / (b - a);
                }
                break;
            }
            case POINT: {
                double a = start + 0.3 * length;
                double b = end - 0.3 * length;
                if (val < a) {
                    FuzzyValue = 0;
                } else if (val > b) {
                    FuzzyValue = 1;
                } else {
                    FuzzyValue = (val - a) / (b - a);
                }
                break;
            }
            default: {
                break;
            }
        }
        return FuzzyValue;
    }
}
