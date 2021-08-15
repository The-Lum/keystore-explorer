; KeyStore Explorer Inno Setup script includes

#define AppID "{A771FBEB-F7E1-4443-9181-AFD57F7BFF45}"
#define AppURL "https://keystore-explorer.org/"
#define AppName ReadIni(AddBackslash(SourcePath) + "appinfo.ini", "Application", "Name")
#define AppCopyright ReadIni(AddBackslash(SourcePath) + "appinfo.ini", "Application", "Copyright")
#define ProgIdName StringChange(AppName, " ", "")
#define AppPublisher ReadIni(AddBackslash(SourcePath) + "appinfo.ini", "Application", "Publisher")
#define AppMajorVersion ReadIni(AddBackslash(SourcePath) + "appinfo.ini", "Application", "Major", "0")
#define AppMinorVersion ReadIni(AddBackslash(SourcePath) + "appinfo.ini", "Application", "Minor", "0")
#define AppPatchVersion ReadIni(AddBackslash(SourcePath) + "appinfo.ini", "Application", "Patch", "0")
#define AppFullVersion AppMajorVersion + "." + AppMinorVersion + "." + AppPatchVersion
#define SourceDir "kse-" + AppMajorVersion + AppMinorVersion + AppPatchVersion
#define SetupAuthor ReadIni(AddBackslash(SourcePath) + "appinfo.ini", "Setup", "Author")
#define SetupVersion AppFullVersion + ".0"
#define AppUpdatesURL ReadIni(AddBackslash(SourcePath) + "appinfo.ini", "Setup", "URL")
#define SetupName "KeyStoreExplorer-" + AppFullVersion + "-setup"
#define SetupCompany SetupAuthor
#define WizardTopImageFilename "setup-55x55.bmp"
#define WizardLeftImageFilename "setup-164x314.bmp"
#define ExeName "kse.exe"
