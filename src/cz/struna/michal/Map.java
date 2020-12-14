package cz.struna.michal;

import oracle.lbs.mapclient.MapViewer;
import oracle.spatial.network.MDPoint;
import oracle.spatial.network.Network;
import oracle.spatial.network.Node;

import java.awt.*;
import java.awt.geom.Point2D;

public class Map {

    private static final String MAP_VIEWER_SERVER = "http://localhost:8080/mapviewer/omserver";

    public static MapViewer viewer;
    public static Network net;
    public static double MAP_SIZE_X;
    public static double MAP_SIZE_Y;
    public static double MAP_OFFSET_X;
    public static double MAP_OFFSET_Y;
    public static double MAP_RATIO;
    public static double SCREEN_SIZE_X;
    public static double SCREEN_SIZE_Y;
    public static double SCREEN_EDGE_X;
    public static double SCREEN_EDGE_Y;

    private Map() {

    }

    public static void setup(Network net) {
        Map.net = net;

        double minX = Integer.MAX_VALUE, maxX = -Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxY = -Integer.MAX_VALUE;

        for (Node n : net.getNodeArray()) {
            double[] coord = n.getMDPoint().getOrd();

            if (coord[0] < minX) minX = coord[0];
            if (coord[0] > maxX) maxX = coord[0];
            if (coord[1] < minY) minY = coord[1];
            if (coord[1] > maxY) maxY = coord[1];

            MAP_SIZE_X = maxX - minX;
            MAP_SIZE_Y = maxY - minY;
            MAP_OFFSET_X = minX;
            MAP_OFFSET_Y = minY;
            MAP_RATIO = MAP_SIZE_X / MAP_SIZE_Y;

            SCREEN_SIZE_X = MAP_RATIO > 1 ? 1600 : 1600 * MAP_RATIO;
            SCREEN_SIZE_Y = MAP_RATIO > 1 ? 1600 / MAP_RATIO : 1600;

            SCREEN_EDGE_X = SCREEN_SIZE_X / 22;
            SCREEN_EDGE_Y = SCREEN_SIZE_Y / 22;
        }

        Map.viewer = getMapViewer();
    }

    private static MapViewer getMapViewer() {
        MapViewer mv = new MapViewer(MAP_VIEWER_SERVER);
        mv.setDataSourceName("idas");
        mv.setDeviceSize(new Dimension((int) Map.SCREEN_SIZE_X, (int) Map.SCREEN_SIZE_Y));
        mv.setBaseMapName("map2");
        mv.setAntiAliasing(true);
        mv.setImageFormat(MapViewer.FORMAT_PNG_URL);

        try { mv.run(); } catch (Exception e) { e.printStackTrace(); }

        return mv;
    }

    public static double normX(double value) {
        return Map.SCREEN_EDGE_X + (value - Map.MAP_OFFSET_X) / Map.MAP_SIZE_X * (Map.SCREEN_SIZE_X - Map.SCREEN_EDGE_X * 2);
    }

    public static double normY(double value) {
        return Map.SCREEN_SIZE_Y - (Map.SCREEN_EDGE_Y + (value - Map.MAP_OFFSET_Y) / Map.MAP_SIZE_Y * (Map.SCREEN_SIZE_Y - 2 * Map.SCREEN_EDGE_Y));
    }

    public static Point2D mdPointToPoint(MDPoint mdPoint, boolean transform) {
        double[] ord = mdPoint.getOrd();
        return new Point2D.Double(transform ? normX(ord[0]) : ord[0],  transform ? normY(ord[1]) : ord[1]);
    }

}
