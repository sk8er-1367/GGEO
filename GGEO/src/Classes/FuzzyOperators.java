
package Classes;

/**
 *
 * @author Sk8er
 */
public class FuzzyOperators {
    private double ItMat[][]= new double[101][101];
    private double TwMat[][]= new double[101][101];
    private final double precision = 0.005;
    public double It(double a,double b){
        return ItMat[Math.round((float) (a*100))][Math.round((float) (b*100))];
    }
    public double Tw(double a,double b){
        return TwMat[Math.round((float) (a*100))][Math.round((float) (b*100))];
    }
    private double ITcalc(int a, int b, double[][] TwMat){
        if(a<=b) {
            return 1.0;
        }
        for(int h=0;h<100;h++){
            if( Math.abs(TwMat[a][h] - (b/100.0)) < precision){
                return h/100.0;
            }
        }
        return 0;
    }
    public double R(MyTile a,MyTile b){
        return R(a,b,0, 4);
    }
    public double R(MyTile a,MyTile b,int alpha, int betha){
        double d = Math.sqrt(Math.pow(a.x-b.x, 2) + Math.pow(a.y-b.y, 2));
        if(d<=alpha) {
            return 1;
        }
        if(d>alpha+betha) {
            return 0;
        }
        return (alpha+betha-d)/betha;
    }

    public FuzzyOperators() {
        for(int i=0;i<101;i++){
            for(int j=0;j<101;j++){
                TwMat[i][j] = Math.max(0, i+j-100)/100.0;                
            }
        }
        for(int i=0;i<101;i++){
            for(int j=0;j<101;j++){
                ItMat[i][j] = ITcalc(i,j,TwMat);
            }
        }
    }
    
}
