package com.ken.kenutils.service.FileComparatorService;

import static com.ken.kenutils.fileUtils.KenFileUtils.writeByteData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.bind.DatatypeConverter;
import com.ken.kenutils.constants.Constants;
import com.ken.kenutils.fileUtils.DecompileUtils;
import com.ken.kenutils.fileUtils.HashUtils;
import com.ken.kenutils.fileUtils.KenFileUtils;

public class FileComparatorServiceImpl implements FileComparatorService{
	
	private ExecutorService  executorService;
    private File logFilePath, dirFile1, dirFile2;
    private Boolean multithreading;
    private boolean  exceptionFlag;
    private long time, endTime;
    
    public AtomicInteger aiProgressCount;
	public Integer fileCount;
    public boolean taskCompleted;
    public String message = null;

    Map<Integer, String> mapFileOnlyExist1;
    Map<Integer, String> mapFilesExclusiveToFileList2;
    Map<Integer, String> mapFile1;
    Map<Integer, String> mapFile2;
    Map<Integer, String> mapFileChecksumDifference;
    Map<Integer, String> mapNonJavaFileDifference;
    Map<Integer, String> mapJavaFileDifference;
   
    public FileComparatorServiceImpl(File dirFile1, File dirFile2, File logFilePath, Boolean multithreading){
    	this.dirFile1 = dirFile1;
    	this.dirFile2 = dirFile2;
    	this.logFilePath = logFilePath;
    	this.multithreading = multithreading;
    	
    }
    
    public AtomicInteger getAiProgressCount() {
		return aiProgressCount;
	}

	public Integer getFileCount() {
		return fileCount;
	}

	public boolean isTaskCompleted() {
		return taskCompleted;
	}

	public String getMessage() {
		return message;
	}
    
    /*public FileComparatorServiceImpl(){
    	this.dirFile1 = new File("C:\\Users\\arun\\Desktop\\Comparison\\production source\\wps p\\WEB-INF\\classes");
    	this.dirFile2 = new File("C:\\Users\\arun\\Desktop\\Comparison\\production source\\wps s\\wps\\build\\classes");
    	this.logFilePath = new File("C:\\Users\\arun\\Desktop\\test1\\new");
    }
    
    public static void main(String[] args){
    	FileComparatorServiceImpl fl = new FileComparatorServiceImpl();
    	fl.generateLog();
    }*/

	public void generateLog() {
		taskCompleted = false;
		exceptionFlag = false;
		aiProgressCount = new AtomicInteger(0);
		fileCount = 0;
		mapFileOnlyExist1 = new TreeMap<>();
		mapFilesExclusiveToFileList2 = new TreeMap<>();
	    mapFile1 = new TreeMap<>();
	    mapFile2 = new TreeMap<>();
	    mapFileChecksumDifference = new TreeMap<>();
	    mapNonJavaFileDifference = new TreeMap<>();
	    mapJavaFileDifference = new TreeMap<>();
		
		Thread workerThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
			    time = System.currentTimeMillis();
		    	List<Future<Integer>> futures = null;
				
				Path logPath = Paths.get(logFilePath.getAbsolutePath());
		        List<File> fileList1 = KenFileUtils.getAllFilesInDirectory(dirFile1, null);
		        List<File> fileList2 = KenFileUtils.getAllFilesInDirectory(dirFile2, null);
		        fileCount = fileList1.size()>fileList2.size() ? fileList1.size() : fileList2.size();
		        
		        if(multithreading){
		        	executorService = Executors.newFixedThreadPool(Constants.CORES);
		        	futures = processMultithreadedWorkload(fileList1, fileList2);
		        	int temp = 0;
		        	for (Future<Integer> future : futures) {
		        		try {
		                    Integer i= future.get();
		                    temp++;
		                    if(i==1){		                            
		                    	System.out.println("A thread has completed executing it's tasks!");
		                    }
		                    if(temp == futures.size()){
		                    	System.out.println("Shutting down executor!");
		                    	executorService.shutdown();
		                    	executorService.awaitTermination(60, TimeUnit.SECONDS);
	                    		logResults(logPath);
		                    }
		    			} catch (Exception e) {
		    				e.printStackTrace();
		    				message = " Exception occured : "+e.getMessage();
		    				exceptionFlag = true;
		    			}
		        	}
		        }
		        else{
		        	processNonMultithreadedWorkload(fileList1, fileList2);
		        	try {
                		logResults(logPath);
					} catch (Exception e) {
						e.printStackTrace();
	    				message = " Exception occured : "+e.getMessage();
	    				exceptionFlag = true;
					}
		        }
			}
		});
		workerThread.start();
	}
	
	public void logResults(Path logPath) throws IOException{

    	if(!exceptionFlag){
    		writeByteData(logPath.resolve("FileList.txt").toString(), 
                    ("\n\n============== File list ("+dirFile1.getAbsolutePath()+") ===================\n\n").getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        	mapFile1.forEach((k,v) -> {
        		if(!exceptionFlag){
        			try {
    					writeByteData(logPath.resolve("FileList.txt").toString(), 
    					        (v+"\n").getBytes(), StandardOpenOption.APPEND);
    				} catch (IOException e) {
    					e.printStackTrace();
    					exceptionFlag = true;
    					message = " Exception occured : "+e.getMessage();
    				}
        		}
    		});
        	
        	writeByteData(logPath.resolve("FileList.txt").toString(), 
                    ("\n\n============== File list ("+dirFile2.getAbsolutePath()+") ===================\n\n").getBytes(),
                    StandardOpenOption.APPEND);
        	mapFile2.forEach((k,v) -> {
        		if(!exceptionFlag){
        			try {
    					writeByteData(logPath.resolve("FileList.txt").toString(), 
    					        (v+"\n").getBytes(), StandardOpenOption.APPEND);
    				} catch (IOException e) {
    					e.printStackTrace();
    					exceptionFlag = true;
    					message = " Exception occured : "+e.getMessage();
    				}
        		}
    		});
        	
        	writeByteData(logPath.resolve("FileExistsOrNot.txt").toString(), 
                    ("\n\n============== Exists only in :"+dirFile1.toString()+" ===================\n\n").getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        	mapFileOnlyExist1.forEach((k,v) -> {
        		if(!exceptionFlag){
        			try {
    					writeByteData(logPath.resolve("FileExistsOrNot.txt").toString(), 
    					        (v+"\n").getBytes(), StandardOpenOption.APPEND);
    				} catch (IOException e) {
    					e.printStackTrace();
    					exceptionFlag = true;
    					message = " Exception occured : "+e.getMessage();
    				}
        		}
    		});
        	
        	writeByteData(logPath.resolve("FileExistsOrNot.txt").toString(), 
                    ("\n\n============== Exists only in :"+dirFile2.toString()+" ===================\n\n").getBytes(),
                    StandardOpenOption.APPEND);
        	mapFilesExclusiveToFileList2.forEach((k,v) -> {
        		if(!exceptionFlag){
        			try {
    					writeByteData(logPath.resolve("FileExistsOrNot.txt").toString(), 
    					        (v+"\n").getBytes(), StandardOpenOption.APPEND);
    				} catch (IOException e) {
    					e.printStackTrace();
    					exceptionFlag = true;
    					message = " Exception occured : "+e.getMessage();
    				}
        		}
    		});
        	
        	writeByteData(logPath.resolve("FileMismatchLog.txt").toString(), 
                    ("\n\n============== File Mismatch ===================\n\n").getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        	mapFileChecksumDifference.forEach((k,v) -> {
        		if(!exceptionFlag){
        			try {
    					writeByteData(logPath.resolve("FileMismatchLog.txt").toString(), 
    					        (v+"\n").getBytes(), StandardOpenOption.APPEND);
    				} catch (IOException e) {
    					e.printStackTrace();
    					exceptionFlag = true;
    					message = " Exception occured : "+e.getMessage();
    				}
        		}
    		});
        	
        	writeByteData(logPath.resolve("NonJavaFileMismatchLog.txt").toString(), 
                    ("\n\n============== Non Java Files Mismatch ===================\n\n").getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        	mapNonJavaFileDifference.forEach((k,v) -> {
        		if(!exceptionFlag){
        			try {
    					writeByteData(logPath.resolve("NonJavaFileMismatchLog.txt").toString(), 
    					        (v+"\n").getBytes(), StandardOpenOption.APPEND);
    				} catch (IOException e) {
    					e.printStackTrace();
    					exceptionFlag = true;
    					message = " Exception occured : "+e.getMessage();
    				}
        		}
    		});
    		
        	writeByteData(logPath.resolve("JavaFileMismatchLog.txt").toString(), 
                    ("\n\n============== Java Files Mismatch ===================\n\n").getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        	mapJavaFileDifference.forEach((k,v) -> {
        		if(!exceptionFlag){
        			try {
    					writeByteData(logPath.resolve("JavaFileMismatchLog.txt").toString(), 
    					        (v+"\n").getBytes(), StandardOpenOption.APPEND);
    				} catch (IOException e) {
    					e.printStackTrace();
    					exceptionFlag = true;
    					message = " Exception occured : "+e.getMessage();
    				}
        		}
    		});
        	
        	//end
            endTime = System.currentTimeMillis() - time;
        	if(!exceptionFlag){
        		message = "Task completed. Time elapsed : " + endTime / 1000.0 + " seconds";
        	}
        	System.out.println(message);
    	}
    	taskCompleted = true;
	}
	
	public List<Future<Integer>> processMultithreadedWorkload(List<File> fileList1,List<File> fileList2){
		List<Future<Integer>> futures = new ArrayList<>();
		for (int thread = 0; thread < Constants.CORES; thread++) {
        	
        	AtomicInteger aiIndex = new AtomicInteger(thread);
        	Callable<Integer> task = () -> {
        		
        		int index = aiIndex.get();
        		try {
        			while (fileCount > index) {
        				if(!exceptionFlag){
	        				
	        				if((fileList1.size()-1)>=index){
	        					processFileList1(fileList1, fileList2, index);
        					}

	        				if((fileList2.size()-1)>=index){
	        					processFileList2(fileList2, fileList1, index);
	        				}
        				}
        				index += Constants.CORES;
        			}
        		} catch (Exception e) {
        			exceptionFlag = true;
        			message = " Exception occured : "+e.getMessage();
        		}
        		return Constants.TASK_COMPLETED;
        	};
        	
        	Future<Integer> future = (Future<Integer>) executorService.submit(task);
            futures.add(future);
        }
		return futures;
	}
	
	public void processNonMultithreadedWorkload(List<File> fileList1,List<File> fileList2){
		int index = 0;
		try {
			while (fileCount > index) {
				if(!exceptionFlag){
    				
    				if((fileList1.size()-1)>=index){
    					processFileList1(fileList1, fileList2, index);
					}

    				if((fileList2.size()-1)>=index){
    					processFileList2(fileList2, fileList1, index);
    				}
				}
				index++;
			}
		} catch (Exception e) {
			exceptionFlag = true;
			message = " Exception occured : "+e.getMessage();
		}
	}
	
	public void processFileList1(List<File> fileList1,List<File> fileList2, int index) throws IOException{
		Path relativePath1 = null;
		Path relativePath2 = null;
		System.out.println(Thread.currentThread()+" is executing task at index "+index+" and path "+fileList1.get(index).getAbsolutePath());
		File file1 = fileList1.get(index);
		File file2 = null;
		Boolean existsPath2 = false;
		mapFile1.put(index, file1.getAbsolutePath());
		relativePath1 = dirFile1.toPath().relativize(file1.toPath()).normalize();
		for(File tempfile2:fileList2){
            relativePath2 = dirFile2.toPath().relativize(tempfile2.toPath()).normalize();
            if(relativePath1.equals(relativePath2)){
                existsPath2 = true;
                file2 = tempfile2;
                break;
            }
        }
		if(!existsPath2){
			mapFileOnlyExist1.put(index, relativePath1.normalize().toString());
			aiProgressCount.incrementAndGet();
        }else{
        	Boolean shaDifference = checkHashDifference(file1, file2, relativePath1, index);
            String tempPath = relativePath1.normalize().toString();
            String extension = null;
            if(tempPath.contains(".")){
            	extension = tempPath.substring(tempPath.lastIndexOf("."), tempPath.length());
            }
            
            if(extension==null || !extension.equals(".class")){
            	Map<String,Object> cmpMap = KenFileUtils.
                		compareFilesLineByLine(file1.getAbsolutePath(), file2.getAbsolutePath());
                Boolean differ = (Boolean) cmpMap.get("differ");
                if(differ){
                	mapNonJavaFileDifference.put(index, relativePath1.normalize().toString());
                }
            }
            else if(extension.equals(".class") && shaDifference){
            	String sourceCode1 = DecompileUtils.decompile(file1.getAbsolutePath());
                String sourceCode2 = DecompileUtils.decompile(file2.getAbsolutePath());
                if(!sourceCode1.equals(sourceCode2)){
                	mapJavaFileDifference.put(index, relativePath1.normalize().toString());
                }
            }
            aiProgressCount.incrementAndGet();
        }
	}
	
	public void processFileList2(List<File> fileList2,List<File> fileList1, int index){

		Path relativePath1 = null;
		Path relativePath2 = null;
		System.out.println(Thread.currentThread()+" is executing task at index "+index+" and path "+fileList2.get(index).getAbsolutePath());
		File file2 = fileList2.get(index);
		Boolean existsPath1 = false;
		mapFile2.put(index, file2.getAbsolutePath());
		relativePath2 = dirFile2.toPath().relativize(file2.toPath()).normalize();
		for(File tempfile1:fileList1){
            relativePath1 = dirFile1.toPath().relativize(tempfile1.toPath()).normalize();
            if(relativePath2.equals(relativePath1)){
                existsPath1 = true;
                break;
            }
        }
		if(!existsPath1){
			mapFilesExclusiveToFileList2.put(index, relativePath2.normalize().toString());
			aiProgressCount.incrementAndGet();
        }
	}
	
	public boolean checkHashDifference(File file1, File file2, Path relativePath1, int index){
		String hexSHAFile1 = DatatypeConverter.printHexBinary(HashUtils.SHA256.checksum(file1));
        String hexSHAFile2 = DatatypeConverter.printHexBinary(HashUtils.SHA256.checksum(file2));
        if(!hexSHAFile1.equals(hexSHAFile2)){
            mapFileChecksumDifference.put(index, relativePath1.normalize().toString());
            return true;
        }
        return false;
	}
	
}
