/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import java.awt.geom.Point2D;
import java.util.HashMap;

/**
 *
 * @author Sk8er
 */
public class MyTile {
    public int x,y;
    
    private double[] values;
    private double[] memberShipFunctionVal = {-1};
    private double area=0;
    HashMap<Integer,String> IFmap;
    HashMap<String, Integer> FImap;
    private double minCordinateX = 0,maxCordinateX=0, minCordinateY=0,maxCordinateY=0;
    private HashMap<String,FeatureGeometryType> features;
    public MyTile(int x, int y, HashMap<String,FeatureGeometryType> features,HashMap<Integer,String> IFmap, HashMap<String, Integer> FImap,double minCordinateX,double maxCordinateX,double minCordinateY, double maxCordinateY) {
        this.features = features;
        this.IFmap = IFmap;
        this.FImap = FImap;
        this.maxCordinateX = maxCordinateX;
        this.maxCordinateY = maxCordinateY;
        this.minCordinateX = minCordinateX;
        this.minCordinateY = minCordinateY;
        this.x = x;
        this.y = y;
        this.memberShipFunctionVal = new double[IFmap.size()];
        this.values = new double[IFmap.size()];
        this.area = (maxCordinateX - minCordinateX) * (maxCordinateY - minCordinateY);        
    }
    public Point2D.Double[] getCoordinates() {
        Point2D.Double[] d= new Point2D.Double[4];
        d[0] = new Point2D.Double();
        d[1] = new Point2D.Double();
        d[2] = new Point2D.Double();
        d[3] = new Point2D.Double();
        
        d[0].x= minCordinateX;
        d[2].x= minCordinateX;
        
        d[0].y=minCordinateY;
        d[1].y = minCordinateY;
        
        d[3].x=maxCordinateX;
        d[1].x= maxCordinateX;
        
        d[3].y=maxCordinateY;
        d[2].y = maxCordinateY;
        
        return d;
    }
    public synchronized double getValues(int featureNum) {
        return values[featureNum];
    }

    public synchronized void setValues(int featureNum ,double values) {
        this.values[featureNum] = values;
    }
    public synchronized void incrementValue(String feature ,double incrementValue) {
        int featureNum = FImap.get(feature);
        this.values[featureNum] += incrementValue;
    }
    private synchronized double CalculateMemberShip(int FN){
        FeatureGeometryType ft = features.get(IFmap.get(FN));
        if(values[FN] < 0.001){
            return 0;
        }
        double start=0,end=0;
        switch (ft) {
            case LINE:
                start=0;
                end= 2 * ((this.maxCordinateX-this.minCordinateX) + (this.maxCordinateY-this.minCordinateY));
                break;
            case POLYGON:
                start = 0;
                end = area;
                break;
            case POINT:
                start = 0;
                end = 10;
                break;
            default:
                break;
        }
        return fuzzifier.getFuzzyValue(values[FN],ft,start, end);
    }
    
    private void setMemberShipFunction(int FN, double m){
        memberShipFunctionVal[FN] = m;
    }
    public double getMemberShipFunction(int FN){
        if(Math.abs(memberShipFunctionVal[FN]  + 1) <0.005) {
            memberShipFunctionVal[FN] = CalculateMemberShip(FN);
            return memberShipFunctionVal[FN];
        }
        return memberShipFunctionVal[FN];
    }
    public void invalidateAllMemberShipFunctions(){
        for(int i=0;i<memberShipFunctionVal.length;i++){
            setMemberShipFunction(i, -1);
        }
    }
    public void invalidateMemberShipFunction(int FN){
        setMemberShipFunction(FN, -1);
    }
    public double getMinCordinateX() {
        return minCordinateX;
    }

    public double getMaxCordinateX() {
        return maxCordinateX;
    }

    public double getMinCordinateY() {
        return minCordinateY;
    }

    public double getMaxCordinateY() {
        return maxCordinateY;
    }
}
