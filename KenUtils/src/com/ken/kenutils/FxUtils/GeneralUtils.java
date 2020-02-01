/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ken.kenutils.FxUtils;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author K3N
 */
public class GeneralUtils {
    
    //define your offsets here
    private static double xOffset = 0;
    private static double yOffset = 0;
    
    private static Parent root;
    private static Stage stage;
    private static Scene scene;
    
    private static Alert alert = new Alert(Alert.AlertType.NONE);
    
    public GeneralUtils(Parent root, Stage stage){
        this.stage = stage;
        this.root = root;
        scene = new Scene(root);

        this.stage.setScene(scene);
    }
    
    public void setMoveWindowOnDrag(boolean moveOnDrag)
    {
        if(moveOnDrag){
            root.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                }
            });

            //move around here
            root.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });
        }
        else{
            root.setOnMousePressed(null);
            root.setOnMouseDragged(null);
        }
    }
    
    public void setStageStyle(StageStyle stageStyle){
        stage.initStyle(stageStyle);
    }
    
    public void setShowStage(Boolean showStage){
        if(showStage){
            this.stage.show();
        }else{
            this.stage.hide();
        }
    }
    
    public static void getAlert(Alert.AlertType alertType,String title,String headertext,String content)
    {
        alert.setAlertType(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headertext);
        alert.setContentText(content);
        alert.show();
    }
    
    public static void showWaitMessage(){
    	getAlert(AlertType.WARNING, "Please wait", null, "Please wait for the process to complete!");
    }
    
}
