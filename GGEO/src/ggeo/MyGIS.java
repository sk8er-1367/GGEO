/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ggeo;

/**
 *
 * @author Sk8er
 */
import java.awt.Color;
import java.net.URL;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.ArrayList;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.geometry.jts.WKTReader2;

public class MyGIS{
    public static void main(String[] args) {
        try {
            usingFeatureCaching();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void usingFeatureCaching() throws Exception {

        //Shape-file's path
        URL url = new URL("file://C:/PE/Italia/reg2011_g.shp");
        ShapefileDataStore shapefile = new ShapefileDataStore(url);

        // Creates a map and adds the shapefile
        MapContent map = new MapContent();

        //Set's windows title
        map.setTitle("Italy");

        //Creates the map style
        StyleBuilder styleBuilder = new StyleBuilder();
        PolygonSymbolizer restrictedSymb = styleBuilder.createPolygonSymbolizer(Color.LIGHT_GRAY, Color.BLACK, 0);

        //Sets opacity
        restrictedSymb.getFill().setOpacity(styleBuilder.literalExpression(0.5));
        org.geotools.styling.Style myStyle = styleBuilder.createStyle(restrictedSymb);

        //Adds another layer to the map     
        FeatureLayer layer = new FeatureLayer(shapefile.getFeatureSource(), myStyle);
        map.addLayer(layer);

        //-------------------------- BUILDS THE CIRCLE ON THE MAP ------------------------------------------//

        StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
        FilterFactory ff = CommonFactoryFinder.getFilterFactory2();

        SimpleFeatureSource fs = shapefile.getFeatureSource();

        SimpleFeatureType pointtype = DataUtilities.createType("Location", "the_geom:Point," + "name:String");

        SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(pointtype);

        double longitude = 537319.7867d;
        double latitude = 5062350.2792d;

        GeometryFactory geometryFactory= JTSFactoryFinder.getGeometryFactory(null);
        com.vividsolutions.jts.geom.Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        sfb.add(point);

        
        
            ArrayList<SimpleFeature> list = new ArrayList<>();
            //WKTReader2 wkt = new WKTReader2();
            //SimpleFeatureType TYPE = DataUtilities.createType("location","geom:Point,name:String");
            //list.add( SimpleFeatureBuilder.build( TYPE, new Object[]{ wkt.read("POINT(1,2)"), "name1"}, null) );
            //list.add( SimpleFeatureBuilder.build( TYPE, new Object[]{ wkt.read("POINT(4,4)"), "name2"}, null) );
            
            //SimpleFeatureCollection col = new ListFeatureCollection(TYPE,list);
        
        SimpleFeatureCollection col = FeatureCollections.newCollection();
        //SimpleFeature feature1 = sfb.buildFeature(null);
        //col.add(feature1);


        org.geotools.styling.Stroke stroke2 = sf.createStroke(
                ff.literal(new Color(0xC8, 0x46, 0x63)),
                //circle thickness
                ff.literal(1)
        );

        org.geotools.styling.Fill fill2 = sf.createFill(
                ff.literal(new Color(0xff, 0xC8, 0x61)));

        map.setTitle("Italia");


        //Type of symbol
        Mark mark = sf.getCircleMark();

        mark.setFill(fill2);
        mark.setStroke(stroke2);

        Graphic graphic = sf.createDefaultGraphic();
        graphic.graphicalSymbols().clear();
        graphic.graphicalSymbols().add(mark);

        //circle dimension on the map
        graphic.setSize(ff.literal(5));

        GeometryDescriptor geomDesc = fs.getSchema().getGeometryDescriptor();
        String geometryAttributeName = geomDesc.getLocalName();
        PointSymbolizer sym2 = sf.createPointSymbolizer(graphic, geometryAttributeName);

        Rule rule2 = sf.createRule();
        rule2.symbolizers().add(sym2);
        Rule rules2[] = {rule2};
        FeatureTypeStyle fts2 = sf.createFeatureTypeStyle(rules2);
        Style style2 = sf.createStyle();
        style2.featureTypeStyles().add(fts2);

        map.addLayer(new FeatureLayer(col, style2));


        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(map);

        //Shows the map 
        JMapFrame.showMap(map);
    }
}
