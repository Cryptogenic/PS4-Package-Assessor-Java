/* This package is distributed under the Apache 2.0 License. */
package pkgAssessor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Creates a stage for the package assessor GUI
 *
 * @author  Specter
 * @version 1.0
 * @since   2016-05-29
 */
public class Main extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainForm.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setTitle("PS4 Package Assessor - GUI Edition");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
