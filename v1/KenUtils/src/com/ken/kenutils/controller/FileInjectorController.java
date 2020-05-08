/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ken.kenutils.controller;

import com.ken.kenutils.FxUtils.GeneralUtils;
import com.ken.kenutils.fileUtils.KenFileUtils;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author K3N
 */
public class FileInjectorController implements Initializable {
    
    @FXML
    private TextField txtSourceDirPath;
    
    @FXML
    private TextField txtDestDirPath;
    
    @FXML
    private Button btnBrowseSourceDir;
    
    @FXML
    private Button btnBrowseDestDir;
    
    @FXML
    private Button btnInject;
    
    @FXML
    private Button btnLoopInject;
    
    @FXML
    private Button btnClose;
    
    @FXML
    private Button btnMinimise;
    
    @FXML
    private TextField txtPeriod;
    
    @FXML
    private TextField txtCount;
    
    @FXML
    private TextField txtVariablePart;
    
    @FXML
    private Label lblInterval;
    
    @FXML
    private Label lblCount;
    
    @FXML
    private Label lblInjected;
    
    private FileChooser fileChooser = new FileChooser();
    private File fileSource = null;
    private File fileDestionation = null;
    private boolean enableLoopInject = false;
    int injected;
    String exMsg = null;
    
    String error = "";
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("initializing...");
        txtPeriod.setVisible(false);
        txtCount.setVisible(false);
        lblInterval.setVisible(false);
        lblCount.setVisible(false);
        
        btnClose.setOnAction(e -> {
            ((Stage)btnMinimise.getScene().getWindow()).close();
            HomeController.primaryStage.show();
            
        });
        
        btnMinimise.setOnAction(e -> {
            ((Stage)btnMinimise.getScene().getWindow()).setIconified(true);
        });
        
        btnBrowseSourceDir.setOnAction(e -> {
        	onSourceDirButtonClick();
        });
        
        btnBrowseDestDir.setOnAction(e -> {
           onDestDirButtonClick();
        });
       
        btnLoopInject.setOnAction(e -> {
        	onEnableLoopInject();
        });
       
       btnInject.setOnAction(e -> {
		   startFileInjector();
        });
       
       System.out.println("Loop Inject: "+enableLoopInject);
    }  
    
    private void onSourceDirButtonClick()
    {
    	DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory containing files to inject");

        fileSource = directoryChooser.
                   showDialog(btnBrowseSourceDir.getScene().getWindow());
        if (fileSource != null) {
            txtSourceDirPath.setText("    "+fileSource.getPath());
        }
    }
    
    private void onDestDirButtonClick()
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Destination Directory");

        fileDestionation = directoryChooser.
                   showDialog(btnBrowseDestDir.getScene().getWindow());
        if (fileDestionation != null) {
            txtDestDirPath.setText("    "+fileDestionation.getPath());
        }
    }
    
    private void onEnableLoopInject()
    {
        String style = "";
        if(enableLoopInject)
        {
        	enableLoopInject = false;
            style = "-fx-text-fill:#f04141;-fx-background-color:#ffff;-fx-background-radius:50px";
            txtPeriod.setVisible(false);
            txtCount.setVisible(false);
            lblInterval.setVisible(false);
            lblCount.setVisible(false);
        }
        else
        {
        	enableLoopInject = true;
            style = "-fx-text-fill:#6cc966;-fx-background-color:#ffff;-fx-background-radius:50px";
            txtPeriod.setVisible(true);
            txtCount.setVisible(true);
            lblInterval.setVisible(true);
            lblCount.setVisible(true);
        }
        btnLoopInject.setStyle(style);
        System.out.println("Inherit directory Structure: "+enableLoopInject);
    }
    
    //new
    private void startFileInjector()
    {
    	if(btnInject.getText().equals("Inject")){
    		
    		injected = 0;
    		btnInject.setText("Injecting");
            Thread injectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    FileFilter filter = new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                           return pathname.isFile();
                        }
                    };
                    
                    try 
                    {
                    	int sleepTime = Integer.parseInt(txtPeriod.getText());
             		    int loopCount = Integer.parseInt(txtCount.getText());
                        for(int i=0;i<loopCount;i++)
                        {
                            File[] files = fileSource.listFiles(filter);
                            
                            for(File file:files)
                            {
                                Path sourceFilepath = Paths.get(file.getPath());
                                Path destinationDirPath = Paths.get(fileDestionation.getPath());

                                Path destinationFilePath = destinationDirPath.resolve(sourceFilepath.getFileName());
                                Files.copy(sourceFilepath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
                                
                                String fileNameAndExt = sourceFilepath.getFileName().toString();
                                String fileName = removeFileExtension(fileNameAndExt);
                                String extension = getFileExtension(fileNameAndExt);
                                String newFileNameAndExt = getIncrementedFileName(fileName)+"."+extension;
                                file.renameTo(new File(sourceFilepath.getParent().resolve(newFileNameAndExt).toString()));
                            }
                            
                            injected+=1;
                            Platform.runLater(new Runnable() {
    				            @Override public void run() {
    				            	lblInjected.setText(""+injected);
    				            }
    				        });
                            Thread.sleep(sleepTime);
                        }
                        Platform.runLater(new Runnable() {
				            @Override public void run() {
				            	GeneralUtils.getAlert(Alert.AlertType.ERROR, "Success", null, "Files injected successfully!");
				            }
				        });
                    } catch (Exception e) {
                    	Platform.runLater(new Runnable() {
				            @Override public void run() {
				            	GeneralUtils.getAlert(Alert.AlertType.ERROR, "Exception", null, "Exception :"+e.getMessage());
				            }
				        });
                    }
                    
                    Platform.runLater(new Runnable() {
			            @Override public void run() {
			            	btnInject.setText("Inject");
			            }
			        });
                }
            });
            injectThread.start();
    	}
        
    }
    
    private String removeFileExtension(String fileNameAndExt)
    {
        String[] array = fileNameAndExt.split("\\.");
        String fileName = array[0];
        return fileName;
    }
    
    private String getFileExtension(String fileNameAndExt)
    {
        String[] array = fileNameAndExt.split("\\.");
        String extension = array[1];
        return extension;
    }
    
    private String zeroAppender(String text, int numOfZerosToAppend)
    {
        StringBuffer sb = new StringBuffer(text);
        for(int i= 0; i<numOfZerosToAppend;i++)
        {
            sb.insert(0, "0");
        }
        return sb.toString();
    }
    private String getIncrementedFileName(String fileName)
    {
        String fileNameFixedPart = fileName.substring(0, fileName.length()-4);
        String fileNameVariablePart = fileName.replace(fileNameFixedPart, "");
        
        int variablePartNum = Integer.parseInt(fileNameVariablePart);
        variablePartNum+=1;
        String variablePartNumStr = ""+variablePartNum;
        
        fileNameVariablePart = zeroAppender(variablePartNumStr, 
                fileNameVariablePart.length()-variablePartNumStr.length());
        
        return fileNameFixedPart+fileNameVariablePart;
    }
    //end
    
}
