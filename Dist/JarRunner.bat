@echo --- k3nrig ---
if not DEFINED IS_MINIMIZED set IS_MINIMIZED=1 && start "" /min "%~dpnx0" %* && exit
call E:\Arun\Tools\Development\jdk8portable\JDK64\bin\java -jar kenutils.jar
exit