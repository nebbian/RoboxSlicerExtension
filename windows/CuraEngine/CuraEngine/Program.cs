using System;
using System.Diagnostics;

namespace SlicerExtension
{
    class CuraEngine
    {
        static void Main(string[] args)
        {
            int exitCode = LaunchCommandLineApp(args);

            Environment.Exit(exitCode);
        }

        static int LaunchCommandLineApp(string[] args)
        {
            try
            {
                using (Process p = new Process())
                {
                    p.StartInfo.CreateNoWindow = true;
                    p.StartInfo.UseShellExecute = false;
                    p.StartInfo.FileName = "..\\..\\AutoMaker\\java\\bin\\java.exe";
                    p.StartInfo.WindowStyle = ProcessWindowStyle.Hidden;
                    p.StartInfo.Arguments = "-jar robox-slicer-flow-1.0-SNAPSHOT.jar " + String.Join(" ", args);
                    p.WaitForExit();
                    return 0;
                }
            }
            catch
            {
                return -1;
            }
        }
    }
}
