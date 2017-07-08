Add-Type -AssemblyName System.Windows.Forms
$f = new-object Windows.Forms.FolderBrowserDialog
$f.Rootfolder = "Desktop"
$f.Description = "Please Select Cel Automaker installation folder"
$Show = $f.ShowDialog()
If ($Show -eq "OK")
{
	$folder = $f.SelectedPath
	$cmd = $folder+"\java\bin\java.exe"
	$prm = '-jar', 'robox-extensions-installer.jar'
	& $cmd $prm
}
Else
{
	Write-Error "Operation cancelled by user."
}