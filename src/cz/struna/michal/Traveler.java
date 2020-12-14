package cz.struna.michal;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import oracle.spatial.network.*;

import java.awt.geom.Point2D;
import java.util.Random;

public class Traveler extends Circle {

    private static final double SPEED = 1;

    private static Random random;

    private Network net;
    private Path path;
    private int step;
    public Text label = new Text();

    static { random = new Random(); }

    public Traveler(Network net, Pane pane) {
        super(100, 100, 5, Color.BLUE);
        pane.getChildren().addAll(this, label);
        this.net = net;
    }

    private Node getRandomNode() {
        return net.getNodeArray()[random.nextInt(net.getNodeArray().length)];
    }

    private void setNewRandomPath() {
        try {
            Node target = getRandomNode();
            Node start = path == null ? getRandomNode() : path.getEndNode();
            Path p = NetworkManager.shortestPath(net, start.getID(), target.getID());

            if (p != null) {
                path = p;
                step = 0;
            }
        } catch (NetworkDataException e) {
            e.printStackTrace();
        }
    }

    public void step(double speed) {
        if (path == null) {
            setNewRandomPath();
            return;
        }

        Point2D start = Map.mdPointToPoint(path.getNodeAt(step).getMDPoint(), true);
        Point2D end = Map.mdPointToPoint(path.getNodeAt(step + 1).getMDPoint(), true);
        Point2D curr = new Point2D.Double(getCenterX(), getCenterY());

        if (curr.equals(end)) {
            if (end.equals(Map.mdPointToPoint(path.getEndNode().getMDPoint(), true))) {
                setNewRandomPath();
            } else {
                step++;
            }

            return;
        }

        double distStartEnd = start.distance(end);
        double distStartCurr = Math.min(start.distance(curr) + SPEED * speed, distStartEnd);
        double t = distStartCurr / distStartEnd;

        Point2D startRaw = Map.mdPointToPoint(path.getNodeAt(step).getMDPoint(), false);
        Point2D endRaw = Map.mdPointToPoint(path.getNodeAt(step + 1).getMDPoint(), false);

        pos.setLocation((1 - t) * startRaw.getX() + t * endRaw.getX(), (1 - t) * startRaw.getY() + t * endRaw.getY());
        setPos((1 - t) * start.getX() + t * end.getX(), (1 - t) * start.getY() + t * end.getY());
        updateText();
    }

    private Point2D pos = new Point2D.Double();

    private void setPos(double x, double y) {
        setCenterX(x);
        setCenterY(y);
        label.setX(x + 10);
        label.setY(y);
    }

    private void updateText() {
        String from = path.getStartNode().getName() + "";
        String to = path.getEndNode().getName();
        String curr = path.getNodeAt(step).getName() + "-" + path.getNodeAt(step + 1).getName();
        label.setText("Z: " + from + "\nDo: " + to + "\nÚsek: " + curr + "\nPozice: " + (Math.round(pos.getX() * 1e3) / 1e3) + ", " + (Math.round((pos.getY() / 1.5) * 1e3) / 1e3));
    }

    public Text getLabel() {
        return label;
    }

    public Path getPath() {
        return path;
    }

}
