/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ken.kenutils.controller;

import com.ken.kenutils.fileUtils.KenFileUtils;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
public class PatchMakerController implements Initializable {
    
    @FXML
    private TextField txtChangelogPath;
    
    @FXML
    private TextField txtDestDirPath;
    
    @FXML
    private Button btnBrowseChangelog;
    
    @FXML
    private Button btnBrowseDestDir;
    
    @FXML
    private Button btnGeneratePatch;
    
    @FXML
    private Button btnInheritDirStr;
    
    
    private FileChooser fileChooser = new FileChooser();
    private Alert alert = new Alert(Alert.AlertType.NONE);
    
    private File resFile= null;
    private List<File> fileArrayList = null;
    private File fileDestionation = null;
    private List<String> invalidPathList = null;
    private List<String> validPathList = null;
    private String changeLogFilePath = null;
    private List<String> changeLogNewFileList = new ArrayList<>();
    private List<String> changeLogUpdateFileList = new ArrayList<>();
    private List<String> changeLogDeleteFileList = new ArrayList<>();
    private Thread fileMonitorThread = null;
    private boolean inheritDirStr = true;
    
    String error = "";
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
		/*
		 * btnClose.setOnAction(e -> {
		 * ((Stage)btnMinimise.getScene().getWindow()).close();
		 * HomeController.primaryStage.show();
		 * 
		 * });
		 */
        
		/*
		 * btnMinimise.setOnAction(e -> {
		 * ((Stage)btnMinimise.getScene().getWindow()).setIconified(true); });
		 */
        
        btnBrowseChangelog.setOnAction(e -> {
           onChangeLogButtonClick();
        });
        
        btnBrowseDestDir.setOnAction(e -> {
           onDestDirButtonClick();
        });
       
       btnInheritDirStr.setOnAction(e -> {
           onInheritDirStrButtonClick();
        });
       
       btnGeneratePatch.setOnAction(e -> {
           onGeneratePatchButtonClick();
        });
       
       System.out.println("Inherit directory Structure: "+inheritDirStr);
    }  
    
    private void onChangeLogButtonClick()
    {
        fileChooser.setTitle("Select Changelog File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
            ,new FileChooser.ExtensionFilter("Temp", "*.xxx")
            );
           
        resFile = fileChooser.
                showOpenDialog(btnBrowseChangelog.getScene().getWindow());
        if(resFile!=null)
        {
            changeLogFilePath = resFile.getPath();
            txtChangelogPath.setText("    "+changeLogFilePath);
        }
    }
    
    private void onDestDirButtonClick()
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory to Save patch");

        fileDestionation = directoryChooser.
                   showDialog(btnBrowseDestDir.getScene().getWindow());
        if (fileDestionation != null) {
            txtDestDirPath.setText("    "+fileDestionation.getPath());
        }
    }
    
    private void onInheritDirStrButtonClick()
    {
        String btnText = btnInheritDirStr.getText();
        String style = "";
        if(btnText.equals("ON"))
        {
            btnInheritDirStr.setText("OFF");
            inheritDirStr = false;
            style = "-fx-text-fill:#f04141;-fx-background-color:#ffff;-fx-background-radius:50px";
        }
        else
        {
            btnInheritDirStr.setText("ON");
            inheritDirStr = true;
            style = "-fx-text-fill:#6cc966;-fx-background-color:#ffff;-fx-background-radius:50px";
        }
        btnInheritDirStr.setStyle(style);
        System.out.println("Inherit directory Structure: "+inheritDirStr);
    }
    
    private void onGeneratePatchButtonClick()
    {
        populateFileList();
        String error = validateChangelogContents();
        if(error==null)
        {
            generatePatch(inheritDirStr);
        }
        else
        {
            getAlert(Alert.AlertType.ERROR, "Error", null, error);
        }
        
    }
    
    private void getAlert(Alert.AlertType alertType,String title,String headertext,String content)
    {
        alert.setAlertType(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headertext);
        alert.setContentText(content);
        alert.show();
    }
    private void populateFileList()
    {
    	try(BufferedReader bufRead = new BufferedReader(new FileReader(changeLogFilePath)))
        {
            fileArrayList = new ArrayList<>();
            validPathList = new ArrayList<>();
            invalidPathList = new ArrayList<>();
            String lineStr = "";
            while((lineStr=bufRead.readLine())!=null)
            {
                lineStr = lineStr.trim();
                if(!lineStr.equals(""))
                {
                    //Path path = Paths.get(lineStr);
                    File fileRes = new File(lineStr);
                    if(fileRes.exists() && fileRes.isFile())
                    {
                        fileArrayList.add(fileRes);
                        //System.out.println(fileRes.getPath()+" >>> is a valid path <<<");
                        validPathList.add(lineStr);
                    }
                    else
                    {
                        //System.out.println(lineStr+" *** is not a file path! ***");
                        invalidPathList.add(lineStr);
                    }
                }
            }
            error ="";
        }
        catch(FileNotFoundException e)
        {
            error = "File not found!";
            //System.out.println("File not found : "+e.getMessage());
            getAlert(Alert.AlertType.ERROR, "Error", null, error);
            
        }
        catch(IOException ex)
        {
            error = "IOException : Check whether you have necessary file permissions!";
            //System.out.println("Exception : "+ex.getMessage());
            getAlert(Alert.AlertType.ERROR, "Error", null, error);
            
        }
        catch(Exception e)
        {
            error = "Error in selected file!";
            //System.out.println("Exception : "+e.getMessage());
            getAlert(Alert.AlertType.ERROR, "Error", null, error);
            
        }
    }
    
    private String validateChangelogContents()
    {
        if(fileArrayList!=null)
        {
            if(fileDestionation==null)
            {
                return "Select a destination directory!";
            }
            else if(!error.equals(""))
            {
                return error;
            }
            else if(fileArrayList.isEmpty())
            {
                return "Nothing to make a patch from...";
            }
            else
            {
            	return null;
            }
        }
        else
        {
            return "No patch resource selected!";
        }
    }
    
    private void generatePatch(boolean inheritDirectoryStructure)
    {
        try 
        {
            for(File file:fileArrayList)
            {
                Path sourceFilePath = Paths.get(file.getPath());
        		Path destinationDirPath = Paths.get(fileDestionation.getPath());
        		
        		      KenFileUtils.copyOrMoveFiles(sourceFilePath, destinationDirPath, inheritDirectoryStructure);
            }
            getAlert(Alert.AlertType.INFORMATION, "Success", null, "Patch generated!");
            
        } catch (Exception e) 
        {
            getAlert(Alert.AlertType.ERROR, "Error", null, "Exception Occured : "+e.getMessage());
        }
        finally
        {
        	viewResultLog();
        }
    
    }
    
    private void viewResultLog()
    {
    	if(!invalidPathList.isEmpty())
    	{
    		JTextArea tArea = new JTextArea(20,50);
    		String invalidPathStr = "Invalid Paths :\n--------------------\n";
    		for(String invStr:invalidPathList)
    		{
    			invalidPathStr += invStr+"\n";
    		}
    		invalidPathStr += "\n--------------------\n";
    		
    		String validPathStr = "\n\nValid Paths :\n--------------------\n";
    		for(String vStr:validPathList)
    		{
    			validPathStr += vStr+"\n";
    		}
    		validPathStr += "\n--------------------\n";

    		tArea.setText(invalidPathStr+validPathStr);
    		
    		Object[] options = {"Copy To Clipboard","Close"};
    		switch(JOptionPane.showOptionDialog(null,
    	            new JScrollPane(tArea),
    	            "Res",
    	            JOptionPane.YES_NO_CANCEL_OPTION,
    	            JOptionPane.DEFAULT_OPTION,
    	            null,
    	            options,
    	            options[1]))
    		{
    			case 0:	StringSelection stringSelection = new StringSelection(tArea.getText());
    					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    					clipboard.setContents(stringSelection, null);
    				break;
    			case 1: 
    				break;
				default: System.exit(0);
    		}
    	}
    }
    
}
