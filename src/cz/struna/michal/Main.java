package cz.struna.michal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import oracle.spatial.network.Network;
import oracle.spatial.network.NetworkDataException;
import oracle.spatial.network.NetworkManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Map.setup(getNetwork());

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Spatial");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, Map.SCREEN_SIZE_X, Map.SCREEN_SIZE_Y));
        primaryStage.show();
    }

    private Connection getConnection() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            return DriverManager.getConnection("jdbc:oracle:thin:@fei-sql1.upceucebny.cz:1521:IDAS", "st52561", "TODO");
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Network getNetwork() {
        try {
            return NetworkManager.readNetwork(getConnection(), "CITY");
        } catch (SQLException | NetworkDataException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
