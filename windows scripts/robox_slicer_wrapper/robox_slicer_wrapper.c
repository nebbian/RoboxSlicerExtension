// A simple wrapper to pass control to the java slicer
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>
#include <Windows.h>
#include <direct.h>

int main (int argc, char *argv[])
{
	FILE *fp;
	int i = 11;
	char outputCommand[1000];
	char logFilePath[1000];
	bool isForEstimation = false;
	char* slic3rPath = "C:/Program Files/Slic3r/slic3r-console.exe";

	char* pwd = _getcwd(NULL, 0);

    sprintf(logFilePath, "%s/slicer_wrapper.log", pwd);

	/* open the log file */
	fp = fopen(logFilePath, "a");
	if (fp == NULL) {
		printf("I couldn't open slicer_wrapper.log for appending.\n");
		exit(0);
	}

	/* write to the log file */
	fprintf(fp, "Starting Slicer wrapper script\n");


	for(i = 1; i < argc; i++){
		fprintf(fp, "%s\n", argv[i]);
		
		if(strstr(argv[i], "TimeCostTemp")){
			isForEstimation = true;
		}
	}


	if (pwd) fprintf(fp, "pwd = %s\n", pwd);


	/* Create the output command */
	
	//sprintf(outputCommand, "\"c:/Program Files/CEL/AutoMaker/java/bin/java.exe\" -jar \"c:/Program Files/CEL/Common/robox-slicer-flow-1.0-SNAPSHOT.jar\"");
	if(isForEstimation){
		sprintf(outputCommand, "cd \"%s\" & \"C:/Program Files/CEL/Common/Cura/CuraEngine_orig.exe\" ", pwd);	
	}
	else {
		//sprintf(outputCommand, "cd \"%s\" & \"%s\" --gui ", pwd, slic3rPath);	
		sprintf(outputCommand, "cd \"%s\" & \"C:/Program Files/CEL/AutoMaker/java/bin/java.exe\" -jar \"c:/Program Files/CEL/Common/robox-slicer-flow-1.0-SNAPSHOT.jar\"", pwd);
	}


	for(i = 1; i < argc; i++){
	
		if((isForEstimation) || (i > 4)){
			sprintf(outputCommand, "%s \"%s\"", outputCommand, argv[i]);
		}
	}
	
	// Save the command to the log file
	fprintf(fp, "%s\n", outputCommand);


	/* close the log file */
	fclose(fp);

	/* Run the command */
	system(outputCommand);

	return 0;
}