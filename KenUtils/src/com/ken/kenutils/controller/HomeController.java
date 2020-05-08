/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ken.kenutils.controller;

import com.ken.kenutils.FxUtils.GeneralUtils;
import com.ken.kenutils.constants.PathConstants;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author K3N
 */
public class HomeController implements Initializable {
    
    @FXML
    private Button btnPatchmaker;
    
    @FXML
    private Button btnFileComparator;
    
    @FXML
    private Button btnFileInjector;
    
    @FXML
    private Button btnJuxtaposer;
    
    public static Stage primaryStage = null;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
		/*
		 * btnClose.setOnAction(e -> { Platform.exit(); });
		 */
        
		/*
		 * btnMinimise.setOnAction(e -> {
		 * ((Stage)btnMinimise.getScene().getWindow()).setIconified(true); });
		 */
        
        btnPatchmaker.setOnAction(e -> {
            openWindow(e, PathConstants.PATCH_MAKER_RESOURCE);
        });
        
        btnFileComparator.setOnAction(e -> {
            openWindow(e, PathConstants.FILE_COMPARATOR_RESOURCE);
        });
       
       btnFileInjector.setOnAction(e -> {
    	   openWindow(e, PathConstants.FILE_INJECTOR_RESOURCE);
        });
       
       btnJuxtaposer.setOnAction(e -> {
    	   openWindow(e, PathConstants.JUXTAPOSER_RESOURCE);
        });
    }

    
    
    private void openWindow(ActionEvent event,String resourcePath){
        try {
            Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
            Stage stage = new Stage();
            GeneralUtils genUtils = new GeneralUtils(root, stage);
            genUtils.setMoveWindowOnDrag(true);
            //genUtils.setStageStyle(StageStyle.UNDECORATED);
            genUtils.setCloseOperation();
            genUtils.setShowStage(Boolean.TRUE);
            
            primaryStage = (Stage)((Node)(event.getSource())).getScene().getWindow();
            primaryStage.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
}
