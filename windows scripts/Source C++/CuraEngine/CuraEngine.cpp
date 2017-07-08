// ConsoleApplication1.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <iostream>
#include <string>
#include <vector>
#include <Windows.h>
#include "Shlwapi.h"
//convertion
#include <atlbase.h>
#include <atlconv.h>
#include <fstream>
#include <direct.h>
#include <stdlib.h>
#include <stdio.h>

using namespace std;

void ReplaceStringInPlace(std::string& subject, const std::string& search,
	const std::string& replace);

//void write_text_to_log_file(const string &text);

std::string get_working_path()
{
	char buf[FILENAME_MAX];
	char* succ = getcwd(buf, FILENAME_MAX);
	if (succ) return std::string(succ);
	return "";
}

void main(int argc, char** argv)
{
	USES_CONVERSION;
	
	string pwd = get_working_path();
	ReplaceStringInPlace(pwd, "\\", "/");

	string logFile = "cppOutput.txt";
	//create log file
	ofstream out(logFile, ios::out);
	//log cout to a file
	freopen(logFile.c_str(), "w", stdout);

	cout << "exe path : ";
	cout << argv[0] << endl;
	cout << "working path : " + pwd << endl;
	//argv[0] is path of current executed file
	string exefilePath = argv[0];
	//remove exe filename from path
	//convert string to LPWSTR
	LPWSTR lp = A2W(exefilePath.c_str());
	PathRemoveFileSpec(lp);
	//back to string
	exefilePath = W2A(lp);
	//cout << "LP : ";
	cout << "exefilePath : "+exefilePath << endl;
	ReplaceStringInPlace(exefilePath,"\\","/");
	
	cout << exefilePath << endl;
	string strFileName;
	strFileName = "\"\"" + exefilePath;
	strFileName += "/../../Automaker/java/bin/java.exe\" ";
	strFileName += "-jar ";
	strFileName += "\"" + exefilePath;
	strFileName += "/../robox-slicer-flow-1.0-SNAPSHOT.jar\" ";

	vector<string> arguments(argv, argv + argc);
	/*for (std::string& s : arguments) {
		strFileName += s+" ";
	}*/
	//cout << "currentWorking dir : "+_getcwd << endl;
	for (std::size_t i = 1; i != arguments.size(); ++i) {
		// access element as v[i]
		string argument = arguments[i];
		ReplaceStringInPlace(argument, "\\", "/");

		if (argument.find(pwd) != std::string::npos) {
			std::cout << "found!" << '\n';
			//replace pwd inside path given
			ReplaceStringInPlace(argument,pwd+"/","");
		}
		strFileName += argument;
		if (i != arguments.size()-1) {
			strFileName += " ";
		}
		//strFileName += "\""+arguments[i] + "\" ";
		// any code including continue, break, return
	}
	strFileName += "\"";

	cout << strFileName << endl;
	//write_text_to_log_file(strFileName);
	system(strFileName.c_str());
	//system("\"C:\\Program Files\\CEL\\AutoMaker\\java\\bin\\java.exe\" -jar \"\"C:\\Program Files\\CEL\\Common\\robox-slicer-flow-1.0-SNAPSHOT.jar\"\"");
	//system("\"C:\\Program Files\\CEL\\AutoMaker\\java\\bin\\java.exe\" -version");

	//wait for user input
	//int age;
	//cin >> age;
}

void ReplaceStringInPlace(std::string& subject, const std::string& search,
	const std::string& replace) {
	size_t pos = 0;
	while ((pos = subject.find(search, pos)) != std::string::npos) {
		subject.replace(pos, search.length(), replace);
		pos += replace.length();
	}
}

//void write_text_to_log_file(const string &text)
//{
//	ofstream log_file(
//		"log_file.txt", ios_base::out | ios_base::app);
//	log_file << text << endl;
//}