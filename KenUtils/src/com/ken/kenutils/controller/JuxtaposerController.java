package com.ken.kenutils.controller;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;
import com.ken.kenutils.FxUtils.GeneralUtils;
import com.ken.kenutils.fileUtils.KenFileUtils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.stage.FileChooser;

public class JuxtaposerController implements Initializable {
    
    @FXML
    private ListView<String> listViewFileDiff;
    
    @FXML
    private ListView<String> listViewFile1;
    
    @FXML
    private ListView<String> listViewFile2;
    
    @FXML
    private ScrollPane scrollPaneFileDiff;
    
    @FXML
    private ScrollPane scrollPaneFile1;
    
    @FXML
    private ScrollPane scrollPaneFile2;
    
    @FXML
    private Button btnPrevious;
    
    @FXML
    private Button btnNext;
    
    @FXML
    private Button btnDiffList;
    
    @FXML
    private Label lblDiffPath;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		adaptListViewToScrollPane(scrollPaneFileDiff, listViewFileDiff);
		adaptListViewToScrollPane(scrollPaneFile1, listViewFile1);
		adaptListViewToScrollPane(scrollPaneFile2, listViewFile2);
        
		
		btnDiffList.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			File selectedFile = fileChooser.showOpenDialog(btnDiffList.getScene().getWindow());

	        if (selectedFile != null) {
	            lblDiffPath.setText(selectedFile.getAbsolutePath());
	            Path pathDiff = Paths.get(selectedFile.getAbsolutePath());
	            Path pathList = Paths.get(selectedFile.getParent()).resolve(Paths.get("FileList.txt"));
	    		populateListView(pathDiff, pathList);
	        }
	        else{
	        	GeneralUtils.getAlert(Alert.AlertType.WARNING, "Warning", null, "No file selected!");
	        }
        });
		
		btnPrevious.setOnAction(e -> {
			int currentIndex = listViewFileDiff.getSelectionModel().getSelectedIndex();
			if(currentIndex!=0) {
				listViewFileDiff.getSelectionModel().clearAndSelect(currentIndex-1);
			}
			else {
				GeneralUtils.getAlert(Alert.AlertType.WARNING, "Warning", null, "Index out of bound!");
			}
        });
		btnNext.setOnAction(e -> {
			int currentIndex = listViewFileDiff.getSelectionModel().getSelectedIndex();
			if(currentIndex<listViewFileDiff.getItems().size()-1) {
				listViewFileDiff.getSelectionModel().clearAndSelect(currentIndex+1);
			}
			else {
				GeneralUtils.getAlert(Alert.AlertType.WARNING, "Warning", null, "Index out of bound!");
			}
        });
	}
	
	public void adaptListViewToScrollPane(ScrollPane scrollPane, ListView<String> listView) {
		ChangeListener<Object> changeListener = new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
                Bounds bounds = scrollPane.getViewportBounds();
                listView.setMinSize(scrollPane.getWidth(), scrollPane.getHeight());
            }
        };
        scrollPane.viewportBoundsProperty().addListener(changeListener);
        scrollPane.hvalueProperty().addListener(changeListener);
        scrollPane.vvalueProperty().addListener(changeListener);
	}
	
	public void populateListView(Path pathDiff, Path pathList) {
		
		try {
			ObservableList<String> obsList = FXCollections.observableList(Files.readAllLines(pathDiff));
			obsList.removeIf(t -> t.trim().equals(""));
			listViewFileDiff.setItems(obsList);
			listViewFileDiff.setEditable(true);
			listViewFileDiff.setCellFactory(TextFieldListCell.forListView());
			listViewFileDiff.getItems().addListener(new ListChangeListener<String>() {
	            @Override
	            public void onChanged(Change<? extends String> change) {
	                while(change.next()){
	                	try {
							KenFileUtils.writeByteData(pathList.getParent().resolve(Paths.get("changelogs.txt")).toString(), ("\n"+change.toString()).getBytes(), StandardOpenOption.APPEND);
						} catch (IOException e) {
							GeneralUtils.getAlert(Alert.AlertType.ERROR, "IOException", null, e.getMessage());
						}
	                }
	            }
	        });
			
			listViewFileDiff.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			    @Override
			    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

		        	String selected = listViewFileDiff.getSelectionModel().getSelectedItem();
		        	listViewFile1.getItems().clear();
		        	listViewFile2.getItems().clear();
		        	try {
						for(String pathStr : Files.readAllLines(pathList)) {
							if(pathStr.startsWith("==============") && listViewFile1.getItems().size() == 0) {
								listViewFile1.getItems().add(pathStr);
							}else if(pathStr.startsWith("==============") && listViewFile2.getItems().size() == 0){
								listViewFile2.getItems().add(pathStr);
							}
							else {
								if(pathStr.contains("\\"+selected)) {
									if(listViewFile1.getItems().size() > 0 && listViewFile2.getItems().size() == 0) {
										listViewFile1.getItems().add(pathStr);
									}
									else {
										listViewFile2.getItems().add(pathStr);
									}
								}
							}
						}
					} catch (IOException e) {
						GeneralUtils.getAlert(Alert.AlertType.ERROR, "IOException", null, e.getMessage());
					}
			    }
			});
			
			listViewFile1.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			    @Override
			    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		        	String selected = listViewFile1.getSelectionModel().getSelectedItem();
		        	StringSelection stringSelection = new StringSelection(selected);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, null);
			    }
			});	
			
			listViewFile2.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			    @Override
			    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		        	String selected = listViewFile2.getSelectionModel().getSelectedItem();
		        	StringSelection stringSelection = new StringSelection(selected);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, null);
			    }
			});
			
		} catch (IOException e) {
			GeneralUtils.getAlert(Alert.AlertType.ERROR, "IOException", null, e.getMessage());
		}
	}
}
