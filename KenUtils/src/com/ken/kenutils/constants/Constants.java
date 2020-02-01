package com.ken.kenutils.constants;

import java.io.File;
import java.nio.file.StandardOpenOption;

public class Constants {

	public static String DB_KENRIG = "kenrig.db";
	
	//STATUS
	public static int TASK_COMPLETED = 0;
	
	
	//MODES
	public static int MODE_LOG_ALL = 0;
	public static int MODE_CREATED_OR_EDITED = 1;
	
	
	//LOG
	public static String MODE_FILE_CREATED ="FILE CREATED : ";
	public static String MODE_FILE_UPDATED ="FILE UPDATED : ";
	public static String MODE_FILE_DELETED ="FILE DELETED : ";
	
	public static Integer CORES = Runtime.getRuntime().availableProcessors();
	
	
}
