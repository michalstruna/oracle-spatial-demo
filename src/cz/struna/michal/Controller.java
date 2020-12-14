package cz.struna.michal;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import oracle.spatial.network.Link;

import java.awt.geom.Point2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private ImageView mapView;

    @FXML
    private Pane objects;

    @FXML
    private Canvas canvas;

    @FXML
    private Slider speedSlider;

    @FXML
    private Slider nTravelersSlider;

    @FXML
    private Label speedLabel;

    @FXML
    private Label nLabel;

    List<Traveler> travelers = new ArrayList<>();

    private Traveler highlighted;

    private GraphicsContext gc;
    private double speed = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image img = new Image(Map.viewer.getGeneratedMapImageURL());
        mapView.setImage(img);

        objects.setOnMouseClicked(event -> {
            highlighted = null;
        });

        nTravelersSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            changeNTRavelers(newValue.intValue());
        });

        changeNTRavelers(5);

        speedSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            speed = Math.round(newValue.doubleValue() * 10.0) / 10.0;
            speedLabel.setText("Rychlost (" + speed + "):");
        }));

        gc = canvas.getGraphicsContext2D();
        canvas.widthProperty().bind(((Pane) canvas.getParent()).widthProperty());
        canvas.heightProperty().bind(((Pane) canvas.getParent()).heightProperty());

        gc.setLineWidth(3);
        gc.setStroke(Color.DARKRED);

        new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    for (Traveler t : travelers) {
                        t.step(speed);
                        t.getLabel().setStroke(Color.DARKBLUE);
                    }

                    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                    if (highlighted != null) {
                        for (Link link : highlighted.getPath().getLinkArray()) {
                            Point2D start = Map.mdPointToPoint(link.getStartNode().getMDPoint(), true);
                            Point2D end = Map.mdPointToPoint(link.getEndNode().getMDPoint(), true);
                            gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
                            highlighted.getLabel().setStroke(Color.DARKRED);
                        }
                    }
                });

                try { Thread.sleep(20); } catch (InterruptedException e) { }
            }
        }).start();
    }

    private void changeNTRavelers(int n) {
        nLabel.setText("Počet aut (" + n + "):");

        for (Traveler t : travelers) {
            ((Pane) t.getParent()).getChildren().removeAll(t, t.getLabel());
        }

        travelers.clear();
        highlighted = null;

        for (int i = 0; i < n; i++) {
            Traveler t = new Traveler(Map.net, objects);
            travelers.add(t);
            t.getLabel().setUserData(t);

            t.getLabel().setOnMouseClicked(event -> {
                highlighted = (Traveler) ((Text) event.getTarget()).getUserData();
                event.consume();
            });
        }
    }

}
