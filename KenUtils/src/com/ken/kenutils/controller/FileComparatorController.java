/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ken.kenutils.controller;

import com.ken.kenutils.FxUtils.GeneralUtils;
import com.ken.kenutils.service.FileComparatorService.FileComparatorServiceImpl;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author K3N
 */
public class FileComparatorController implements Initializable {

    @FXML
    private TextField txtDirPath1;
    
    @FXML
    private TextField txtDirPath2;
    
    @FXML
    private Button btnBrowseDirPath1;
    
    @FXML
    private Button btnBrowseDirPath2;
    
    @FXML
    private Button btnGenerateLog;
    
    @FXML
    private Button btnMultithreading;
    
    @FXML
    private ProgressBar pBar;
    
    private static boolean multithreading = false;
    private static File dirFile1 = null;
    private static File dirFile2 = null;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    	btnGenerateLog.setText("Generate Patch");
        
        btnBrowseDirPath1.setOnAction(e -> {
            onDirSelect1();
        });
        
        btnBrowseDirPath2.setOnAction(e -> {
            onDirSelect2();
        });
        
        btnGenerateLog.setOnAction(e -> {
        	generateLog();
        });
        
        btnMultithreading.setOnAction(e -> {
        	multithreadingButtonClick();
        });
    }    
    
    public void onDirSelect1(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Primary Directory");

        dirFile1 = directoryChooser.
                   showDialog(btnBrowseDirPath1.getScene().getWindow());
        if (dirFile1 != null) {
            txtDirPath1.setText("    "+dirFile1.getPath());
        }
        else{
        	txtDirPath1.setText("");
        	txtDirPath1.setPromptText("    --- Select directory 1 ---");
        }
    }
    
    public void onDirSelect2(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Secondary Directory");

        dirFile2 = directoryChooser.
                   showDialog(btnBrowseDirPath2.getScene().getWindow());
        if (dirFile2 != null) {
            txtDirPath2.setText("    "+dirFile2.getPath());
        }
        else{
        	txtDirPath2.setText("");
        	txtDirPath1.setPromptText("    --- Select directory 2 ---");
        }
    }
    
    private void multithreadingButtonClick(){
        String btnText = btnMultithreading.getText();
        String style = "";
        if(btnText.equals("ON"))
        {
            btnMultithreading.setText("OFF");
            multithreading = false;
            style = "-fx-text-fill:#f04141;-fx-background-color:#ffff;-fx-background-radius:50px";
        }
        else
        {
            btnMultithreading.setText("ON");
            multithreading = true;
            style = "-fx-text-fill:#6cc966;-fx-background-color:#ffff;-fx-background-radius:50px";
        }
        btnMultithreading.setStyle(style);
        System.out.println("Uncompress Archive : "+multithreading);
    }
    
    public void generateLog(){

        String btnText = btnGenerateLog.getText();
        if(btnText.equals("Generate Patch"))
        {
        	DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Directory to Save the log");

            File logFilePath = directoryChooser.
                       showDialog(btnBrowseDirPath2.getScene().getWindow());
            if (dirFile1 !=null && dirFile2 !=null && logFilePath != null) {
            	toggleButtonStatus(true);
            	btnGenerateLog.setText("Stop");
            	FileComparatorServiceImpl fcService = new FileComparatorServiceImpl(dirFile1,
            			dirFile2, logFilePath, multithreading);
            	fcService.generateLog();
            	TimerTask timerTask = new TimerTask() {
					
					@Override
					public void run() {
						updateProgressBar(fcService.getAiProgressCount(), fcService.getFileCount());
						if(fcService.isTaskCompleted()){
							Platform.runLater(new Runnable() {
					            @Override public void run() {
					            	GeneralUtils.getAlert(Alert.AlertType.INFORMATION, "Success", null, fcService.getMessage());
					            	 btnGenerateLog.setText("Generate Patch");
					            	 toggleButtonStatus(false);
					            }
					        });
			            	this.cancel();
						}
					}
				};
				Timer timer = new Timer();
				timer.schedule(timerTask, 0, 400);
            }
            else if(dirFile1 ==null || dirFile2 ==null){
            	GeneralUtils.getAlert(Alert.AlertType.INFORMATION, "Success", null, "Please select directories to compare!");
            }
            else if(logFilePath == null){
            	GeneralUtils.getAlert(Alert.AlertType.INFORMATION, "Success", null, "No directory select to save the results!");
            }
        }
        else
        {
        	GeneralUtils.getAlert(Alert.AlertType.INFORMATION, "Success", null, "Please wait! Task cannot be interrupted.!");
        }
    }
    
    public void updateProgressBar(AtomicInteger aiProgressCount, Integer fileCount){
    	Platform.runLater(() -> pBar.setProgress((Double.valueOf(aiProgressCount.get()) / Double.valueOf(fileCount))));
    }
    
    public void toggleButtonStatus(Boolean disabled){
    	Platform.runLater(new Runnable() {
            @Override public void run() {
            	btnBrowseDirPath1.setDisable(disabled);
            	btnBrowseDirPath2.setDisable(disabled);
            	btnMultithreading.setDisable(disabled);
            	if(disabled){
            		pBar.setProgress(0);
            	}
            }
        });
    }
}
