
package ggeo;

import Classes.AreaIntersect;
import Classes.FeatureGeometryType;
import Classes.MyNet;
import Classes.MyTile;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.hsqldb.store.BitMap;

/**
 *
 * @author Sk8er
 */
public class Run {
    int aaa=0;
    ExecutorService exec = Executors.newFixedThreadPool(10);
    
    private class JobGeometryAreaLength implements Callable<Boolean> {

        ArrayList<Geometry> gl;
        String featureName;
        MyNet gridNet;
        HashMap<String, FeatureGeometryType> FGmap;

        public JobGeometryAreaLength(ArrayList<Geometry> gl, String featureName, MyNet gridNet, HashMap<String, FeatureGeometryType> FGmap) {
            this.gl = gl;
            this.featureName = featureName;
            this.gridNet = gridNet;
            this.FGmap = FGmap;
        }

        Point2D.Double getLineIntersection(Line2D.Double l1,
                Line2D.Double l2) {
            Point2D.Double intersection = new Point2D.Double(0, 0);
            if (!l1.intersectsLine(l2)) {
                return null;
            }

            double x1 = l1.getX1(), y1 = l1.getY1(),
                    x2 = l1.getX2(), y2 = l1.getY2(),
                    x3 = l2.getX1(), y3 = l2.getY1(),
                    x4 = l2.getX2(), y4 = l2.getY2();

            intersection.x = det(det(x1, y1, x2, y2), x1 - x2,
                    det(x3, y3, x4, y4), x3 - x4)
                    / det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
            intersection.y = det(det(x1, y1, x2, y2), y1 - y2,
                    det(x3, y3, x4, y4), y3 - y4)
                    / det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);

            if (((x1 < intersection.x && x2 > intersection.x) || (x1 > intersection.x && x2 < intersection.x))
                    && ((y1 < intersection.y && y2 > intersection.y) || (y1 > intersection.y && y2 < intersection.y))
                    && ((x3 < intersection.x && x4 > intersection.x) || (x3 > intersection.x && x4 < intersection.x))
                    && ((y3 < intersection.y && y4 > intersection.y) || (y3 > intersection.y && y4 < intersection.y))) {
                return intersection;
            }
            return null;
        }

        double det(double a, double b, double c, double d) {
            return a * d - b * c;
        }

        @Override
        public Boolean call() throws Exception {
            ExecutorService execI = Executors.newFixedThreadPool(8);
            for (final Geometry g : gl) {
                execI.execute(new Runnable() {
                    @Override
                    public void run() {

                        Coordinate[] coordinates = g.getCoordinates();
                        BitMap[] bm = new BitMap[gridNet.x*10];
                        for (int i = 0; i < gridNet.x*10; i++) {
                            bm[i] = new BitMap(gridNet.y*10);
                        }
                        for(Coordinate co:coordinates){
                            gridNet.max
                        }
                        for (int i = 0; i < gridNet.x; i++) {
                            for (int j = 0; j < gridNet.y; j++) {
                                MyTile t = gridNet.getTile(i, j);
                                if (FGmap.get(featureName) == FeatureGeometryType.POLYGON) {
                                    double CalculatedArea = AreaIntersect.Calc(coordinates, tc);
                                    if(CalculatedArea > 0.000001)
                                    {
                                        t.incrementValue(featureName, CalculatedArea);
                                    }
                                    
                                } else {
                                    Line2D.Double[] l = new Line2D.Double[4];
                                    Point2D[] tCoordinates = t.getCoordinates();
                                    l[0] = new Line2D.Double(tCoordinates[0], tCoordinates[1]);
                                    l[1] = new Line2D.Double(tCoordinates[2], tCoordinates[3]);
                                    l[2] = new Line2D.Double(tCoordinates[0], tCoordinates[2]);
                                    l[3] = new Line2D.Double(tCoordinates[1], tCoordinates[3]);
                                    Point2D.Double start = null, end = null;
                                    double length = 0;
                                    for (int k = 0; k < coordinates.length - 1; k++) {

                                        Line2D.Double lmain = new Line2D.Double(new Point2D.Double(coordinates[k].x, coordinates[k].y), new Point2D.Double(coordinates[k + 1].x, coordinates[k + 1].y));
                                        Point2D.Double intp;
                                        for (int m = 0; m < 4; m++) {
                                            intp = getLineIntersection(l[m], lmain);
                                            if (intp != null) {
                                                if (start == null) {
                                                    start = intp;
                                                } else {
                                                    if (end != null) {
                                                        start = intp;
                                                        end = null;
                                                    } else {
                                                        end = intp;
                                                        double d = Point2D.Double.distance(start.x, start.y, end.x, end.y);
                                                        length += d;
                                                    }
                                                }
                                            }
                                        }
                                        if (start != null && end != null) {
                                            start = end = null;
                                        }
                                        if (start != null) {
                                            double d = Point2D.Double.distance(start.x, start.y, coordinates[i + 1].x, coordinates[i + 1].y);
                                            start = new Point2D.Double(coordinates[i + 1].x, coordinates[i + 1].y);
                                            length += d;
                                        }

                                    }
                                    if(length > 0.00001){
                                        aaa++;
                                        t.incrementValue(featureName, length);
                                    }
                                }
                            }
                        }
                    }
                });
                
            }
            execI.shutdown();
            execI.awaitTermination(1, TimeUnit.DAYS);
            return true;
        }
    }

    private class JobMemberShips implements Callable<Boolean> {

        MyNet gridNet;
        String feature;

        public JobMemberShips(String feature, MyNet gridNet) {
            this.feature = feature;
            this.gridNet = gridNet;
        }

        @Override
        public Boolean call() throws Exception {
            gridNet.calculateMemberShipes(feature);
            return true;
        }
    }

    private class JobOverlapRCCFuzzy implements Callable<Double> {

        MyNet gridNet;
        String feature1, feature2;

        public JobOverlapRCCFuzzy(String feature1, String feature2, MyNet gridNet) {
            this.feature1 = feature1;
            this.feature2 = feature2;
            this.gridNet = gridNet;
        }

        @Override
        public Double call() throws Exception {
            return gridNet.fuzzyOverLap(feature1, feature2);

        }
    }

    ArrayList<Geometry> readGeomFromShp(String shpfile) {
        ArrayList<Geometry> gl = null;
        try {
            ShpFiles shapeFile = new ShpFiles(shpfile);
            GeometryFactory gf = new GeometryFactory();
            ShapefileReader sfr;
            sfr = new ShapefileReader(shapeFile, true, true, gf);
            gl = new ArrayList<>();
            while (sfr.hasNext()) {
                ShapefileReader.Record record = sfr.nextRecord();
                Geometry g = (Geometry) record.shape();
                gl.add(g);
            }
            sfr.close();
        } catch (Exception ex) {
            Logger.getLogger(Run.class.getName()).log(Level.SEVERE, null, ex);
        }
        return gl;
    }

    public void runMe() {
        try {
            double[] fuz;

            double minX = 0, minY = 0, maxX = 0, maxY = 0;
            ShpFiles shapeFile = new ShpFiles("files/IRN_adm0.shp");//whole iranian territory
            GeometryFactory gf = new GeometryFactory();
            ShapefileReader sfr;
            sfr = new ShapefileReader(shapeFile, true, true, gf);
            while (sfr.hasNext()) {
                ShapefileReader.Record nextRecord = sfr.nextRecord();
                maxX = nextRecord.maxX;
                maxY = nextRecord.maxY;
                minX = nextRecord.minX;
                minY = nextRecord.minY;
            }
            sfr.close();
            
            HashMap<String, FeatureGeometryType> FGmap = new HashMap<>(650);
            shapeFile = new ShpFiles("files/IRN_adm2.shp");//whole iranian territory
            gf = new GeometryFactory();
            sfr = new ShapefileReader(shapeFile, true, true, gf);
            int cityNum=0;
            Geometry[] citiesGeom = new Geometry[650];
            while (sfr.hasNext()) {
                ShapefileReader.Record nextRecord = sfr.nextRecord();
                FGmap.put("_"+cityNum, FeatureGeometryType.POLYGON);
                citiesGeom[cityNum]=(Geometry)nextRecord.shape();
                cityNum++;
            }
            sfr.close();

            
            
            
            FGmap.put("city", FeatureGeometryType.POLYGON);
            FGmap.put("road", FeatureGeometryType.LINE);
            FGmap.put("river", FeatureGeometryType.LINE);
            FGmap.put("rail", FeatureGeometryType.LINE);
            FGmap.put("veg", FeatureGeometryType.POLYGON);
            FGmap.put("mount", FeatureGeometryType.POLYGON);
            FGmap.put("urban", FeatureGeometryType.POLYGON);
            MyNet gridNet = new MyNet(70, 70, FGmap, minX, maxX, minY, maxY);

            long startTime = System.currentTimeMillis();
            System.out.println("Started at: " + startTime);

            List<Callable<Boolean>> tasks = new ArrayList<>();
            //cities
            for(int k=0;k<cityNum;k++){
                ArrayList<Geometry> ggg = new ArrayList<>(1);
                ggg.add(citiesGeom[k]);
                tasks.add(new JobGeometryAreaLength(ggg, "_"+k, gridNet, FGmap));
            }
            tasks.add(new JobGeometryAreaLength(readGeomFromShp("files/irRoads.shp"), "road", gridNet, FGmap));
            tasks.add(new JobGeometryAreaLength(readGeomFromShp("files/iran_waterways.shp"), "river", gridNet, FGmap));
            tasks.add(new JobGeometryAreaLength(readGeomFromShp("files/irRailRoads.shp"), "rail", gridNet, FGmap));
            tasks.add(new JobGeometryAreaLength(readGeomFromShp("files/iran_natural.shp"), "veg", gridNet, FGmap));
            tasks.add(new JobGeometryAreaLength(readGeomFromShp("files/Plain-Mountain.shp"), "mount", gridNet, FGmap));
            tasks.add(new JobGeometryAreaLength(readGeomFromShp("files/irUrbanAreas.shp"), "urban", gridNet, FGmap));
            long endTime = System.currentTimeMillis();
            long seconds = (endTime - startTime);
            startTime = endTime;
            System.out.println("Data read length: " + seconds + "\nCalculations started at at: " + startTime);
            try {
                List<Future<Boolean>> results = exec.invokeAll(tasks);
                for (Future<Boolean> result : results) {
                    if (!result.get().booleanValue()) {
                        throw new RuntimeException();
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            endTime = System.currentTimeMillis();
            seconds = (endTime - startTime);
            startTime = endTime;
            System.out.println("phase1 Time in milliseconds: " + seconds);
            tasks = new ArrayList<>();
            //tasks.add(new JobMemberShips("city", gridNet));
            //cities
            for(int k=0;k<cityNum;k++){
                tasks.add(new JobMemberShips("_"+k, gridNet));
            }
            tasks.add(new JobMemberShips("road", gridNet));
            tasks.add(new JobMemberShips("rail", gridNet));
            tasks.add(new JobMemberShips("urban", gridNet));
            tasks.add(new JobMemberShips("river", gridNet));
            tasks.add(new JobMemberShips("veg", gridNet));
            tasks.add(new JobMemberShips("mount", gridNet));
            try {
                List<Future<Boolean>> results = exec.invokeAll(tasks);
                for (Future<Boolean> result : results) {
                    if (!result.get().booleanValue()) {
                        throw new RuntimeException();
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            endTime = System.currentTimeMillis();
            seconds = (endTime - startTime);
            startTime = endTime;
            System.out.println("phase2 Time in milliseconds: " + seconds);
            List<Callable<Double>> tasks3 = new ArrayList<>();
            //cities
            for(int k=0;k<cityNum;k++){
                tasks3.add(new JobOverlapRCCFuzzy("_"+k, "urban", gridNet));
                tasks3.add(new JobOverlapRCCFuzzy("_"+k, "road", gridNet));
                tasks3.add(new JobOverlapRCCFuzzy("_"+k, "river", gridNet));
                tasks3.add(new JobOverlapRCCFuzzy("_"+k, "rail", gridNet));
                tasks3.add(new JobOverlapRCCFuzzy("_"+k, "veg", gridNet));
                tasks3.add(new JobOverlapRCCFuzzy("_"+k, "mount", gridNet));
            }
            
            try {
                List<Future<Double>> results = exec.invokeAll(tasks3);
                fuz = new double[tasks3.size()];
                for (int k = 0; k < tasks3.size(); k++) {
                    fuz[k] = results.get(k).get().doubleValue();
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            endTime = System.currentTimeMillis();
            seconds = (endTime - startTime);
            System.out.println("phase3 Time in milliseconds: " + seconds);
            for (int k = 0; k < tasks3.size(); k++) {
                System.out.println(fuz[k]);
            }
        } catch (IOException ex) {
            Logger.getLogger(Run.class.getName()).log(Level.SEVERE, null, ex);
        }
        exec.shutdown();
        
    }
}
