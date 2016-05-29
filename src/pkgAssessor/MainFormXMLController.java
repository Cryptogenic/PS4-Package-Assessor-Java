/* This package is distributed under the Apache 2.0 License. */
package pkgAssessor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Contains the implementation for the XML GUI interface.
 *
 * @author  Specter
 * @version 1.0
 * @since   2016-05-29
 */
public class MainFormXMLController implements Initializable {
    
    /* Handles for all components we need to manipulate for showing package info */
    @FXML private Label packageContentID;
    @FXML private Label packageSize;
    @FXML private Label packageDataSize;
    @FXML private Label packageDataOffset;
    @FXML private Label magicCnt;
    @FXML private Label magicPkg;
    @FXML private Label magicUnknown;
    
    /* ListView for displaying package contents */
    @FXML private ListView<String> packageContents;
    
    /* Contains package contents as an array */
    private final ObservableList<String> packageData = 
            FXCollections.observableArrayList();
    
    /* Stores the file loaded for evaluation */
    private File pFile;
    
    /**
     * Handles the button event to load a package into the program
     * <p>
     * @author Specter
     * @param  event  component-defined action registered
     */
    @FXML
    private void handleLoadPackageBtn(ActionEvent event) {
        System.out.println("Launching File Chooser for .pkg file...");
        
        /* Open a file dialog for the user to load .pkg file */
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open .PKG File");
        pFile = fileChooser.showOpenDialog(packageContentID.getScene().getWindow());
        
        /* Attempt to evaluate package, if an exception occurs, catch it */
        try
        {
            /* Get Package Information and store in packageInfo */
            ArrayList<String> packageInfo = Utilities.evaluatePackage(pFile);
            
            /* Switch on file magic */
            switch (packageInfo.get(1)) {
                case "CNT":
                    magicCnt.setOpacity(1);
                    magicPkg.setOpacity(0);
                    magicUnknown.setOpacity(0);
                    break;
                case "PKG":
                    magicCnt.setOpacity(0);
                    magicPkg.setOpacity(1);
                    magicUnknown.setOpacity(0);
                    break;
                default:
                    magicCnt.setOpacity(0);
                    magicPkg.setOpacity(0);
                    magicUnknown.setOpacity(1);
                    break;
            }
            
            /* If the package is valid, set labels to package information */
            if(packageInfo.get(1).equals("CNT") || packageInfo.get(1).equals("PKG"))
            {
                /* Clear out the contents list when loading a new package */
                packageData.clear();
                packageContents.setItems(packageData);
                
                /* Set labels to the package information */
                packageContentID.setText(packageInfo.get(2));
                packageSize.setText(packageInfo.get(3));
                packageDataSize.setText(packageInfo.get(4));
                packageDataOffset.setText(packageInfo.get(5));
                
                /* Allow for up to 20 package contents, indexes 6-26 */
                for(int i = 6; i < 28; i++)
                {
                    /* i + 1 to remove garbage data at the end of the list */
                    if(!(i + 1 >= packageInfo.size()))
                    {
                        packageData.add(packageInfo.get(i));
                        packageContents.setItems(packageData);
                    }
                }
            }
        }
        catch(IOException ioe)
        {
            /* An exception occured while reading the file, print stacktrace for debug */
            System.out.println("Encountered an IO exception while opening the file. More information: \n");
            ioe.printStackTrace();
        }
        catch(Exception e)
        {
            /* An exception occured from somewhere else, print stacktrace for debug */
            System.out.println("Encountered an unknown exception while opening the file. More information: \n");
            e.printStackTrace();
        }
    }
    
    /**
     * Initializes the stage and sets default values
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        magicCnt.setOpacity(0);
        magicPkg.setOpacity(0);
        magicUnknown.setOpacity(0);
    }    
    
}
