
package Classes;

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Sk8er
 */
public class MyNet {

    private MyTile[][] tiles;
    public int x = 0,y=0;
    private int minCordinateX = 0,maxCordinateX=0, minCordinateY=0,maxCordinateY=0;
    HashMap<Integer,String> IFmap;
    HashMap<String,Integer> FImap;
    public MyTile getTile(int x,int y){
        return tiles[x][y];
    }
   
    public MyNet(int dx, int dy,HashMap<String,FeatureGeometryType> features ,double minCordinateX,double maxCordinateX,double minCordinateY, double maxCordinateY) {
        
        this.maxCordinateX = (int)(maxCordinateX*100000);
        this.maxCordinateY = (int)(maxCordinateY*100000);
        int currentCordinateX = this.minCordinateX = (int)(minCordinateX*100000);
        int currentCordinateY = this.minCordinateY = (int)(minCordinateY*100000);
        this.x = dx;
        this.y = dy;
        this.tiles = new MyTile[x][y];
        IFmap = new HashMap<>(features.size());
        FImap = new HashMap<>(features.size());
        Iterator<String> iterator = features.keySet().iterator();
        Integer k =0;
        while(iterator.hasNext()){
            String val = iterator.next();
            IFmap.put(k,val);
            FImap.put(val,k);
            k++;
        }
        
        for (int i = 0; i < x; i++) {
            int nextCordinateX = currentCordinateX + ((this.maxCordinateX-this.minCordinateX)/x);
            if(i==x-1) //last one
                {
                    nextCordinateX = this.maxCordinateX;
                }
            for (int j = 0; j < y; j++) {
                int nextCordinateY = currentCordinateY + ((this.maxCordinateY-this.minCordinateY)/y);
                if(j==y-1) //last one
                {
                    nextCordinateY = this.maxCordinateY;
                }
                this.tiles[i][j] = new MyTile(i, j, features,IFmap,FImap,currentCordinateX,nextCordinateX,currentCordinateY,nextCordinateY);
                currentCordinateY = nextCordinateY;
            }
            currentCordinateX = nextCordinateX;
        }
        

    }
    public void calculateMemberShipes(String f1){
        for (int i = 0; i < this.x; i++) {
            for (int j = 0; j < this.y; j++) {
                tiles[i][j].invalidateMemberShipFunction(FImap.get(f1));
                tiles[i][j].getMemberShipFunction(FImap.get(f1));
            }
        }
    }

    public double fuzzyOverLap(String f1, String f2) {
        int ft1 = FImap.get(f1);
        int ft2 = FImap.get(f2);
        FuzzyOperators fo = new FuzzyOperators();
        double SupTw1 = 0, SupTw2 = 0,
                infIt1 = 1, infIt2 = 1;
        for (int i = 0; i < this.x; i++) {
            for (int j = 0; j < this.y; j++) {
                for (int k = 0; k < this.x; k++) {
                    for (int l = 0; l < this.y; l++) {
                        SupTw1 = Math.max(SupTw1, fo.Tw(fo.R(tiles[i][j], tiles[k][l]), tiles[i][j].getMemberShipFunction(ft1)));
                        SupTw2 = Math.max(SupTw2, fo.Tw(fo.R(tiles[i][j], tiles[k][l]), tiles[i][j].getMemberShipFunction(ft2)));
                    }
                }
            }
        }
        for (int i = 0; i < this.x; i++) {
            for (int j = 0; j < this.y; j++) {
                for (int k = 0; k < this.x; k++) {
                    for (int l = 0; l < this.y; l++) {
                        infIt1 = Math.min(SupTw1, fo.It(fo.R(tiles[i][j], tiles[k][l]), SupTw1));
                        infIt2 = Math.min(SupTw2, fo.It(fo.R(tiles[i][j], tiles[k][l]), SupTw2));
                    }
                }
            }
        }
        return fo.Tw(infIt1, infIt2);
    }

    public int getMinCordinateX() {
        return minCordinateX;
    }

    public int getMaxCordinateX() {
        return maxCordinateX;
    }

    public int getMinCordinateY() {
        return minCordinateY;
    }

    public int getMaxCordinateY() {
        return maxCordinateY;
    }
}
