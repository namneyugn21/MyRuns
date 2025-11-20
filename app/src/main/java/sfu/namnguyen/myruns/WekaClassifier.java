package sfu.namnguyen.myruns;

class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        double p = Double.NaN;
        p = WekaClassifier.Ndb94f854(i);
        return p;
    }
    static double Ndb94f854(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() <= 13.390311) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() > 13.390311) {
            p = WekaClassifier.N420bbfaf5(i);
        }
        return p;
    }
    static double N420bbfaf5(Object []i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 1;
        } else if (((Double) i[64]).doubleValue() <= 14.534508) {
            p = WekaClassifier.N123187596(i);
        } else if (((Double) i[64]).doubleValue() > 14.534508) {
            p = 2;
        }
        return p;
    }
    static double N123187596(Object []i) {
        double p = Double.NaN;
        if (i[4] == null) {
            p = 1;
        } else if (((Double) i[4]).doubleValue() <= 14.034383) {
            p = WekaClassifier.N43336cb7(i);
        } else if (((Double) i[4]).doubleValue() > 14.034383) {
            p = 1;
        }
        return p;
    }
    static double N43336cb7(Object []i) {
        double p = Double.NaN;
        if (i[7] == null) {
            p = 1;
        } else if (((Double) i[7]).doubleValue() <= 4.804712) {
            p = 1;
        } else if (((Double) i[7]).doubleValue() > 4.804712) {
            p = 2;
        }
        return p;
    }
}
