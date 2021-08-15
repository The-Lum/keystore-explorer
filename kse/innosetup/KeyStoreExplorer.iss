; KeyStore Explorer Inno Setup script

#if Ver < EncodeVer(6,0,0,0)
  #error This script requires Inno Setup 6 or later
#endif

#include AddBackslash(SourcePath) + "includes.iss"

; Use preprocessor to iterate file extenions
; Update appinfo.ini [File Types] section to match
#define protected NumExtensions 8
#dim protected Extensions[NumExtensions]
#define protected Extensions[0] ".jceks"
#define protected Extensions[1] ".jks"
#define protected Extensions[2] ".keystore"
#define protected Extensions[3] ".ks"
#define protected Extensions[4] ".p12"
#define protected Extensions[5] ".pfx"
#define protected Extensions[6] ".bks"
#define protected Extensions[7] ".uber"
#define protected i 0

[Setup]
AllowNoIcons=yes
AppId={{#AppID}
AppName={#AppName}
AppPublisher={#AppPublisher}
AppPublisherURL={#AppURL}
AppCopyright={#AppCopyright}
AppVersion={#AppFullVersion}
ChangesAssociations=yes
CloseApplications=yes
CloseApplicationsFilter=*.exe,*.jar
DefaultDirName={autopf}\{#AppName}
DefaultGroupName={#AppName}
DisableWelcomePage=no
MinVersion=6.1sp1
OutputBaseFilename={#SetupName}
OutputDir=.
PrivilegesRequired=admin
PrivilegesRequiredOverridesAllowed=dialog
SetupIconFile=icons\kse.ico
SolidCompression=yes
Compression=lzma2/ultra64
LZMAUseSeparateProcess=yes
LZMADictionarySize=1048576
LZMANumFastBytes=273
UninstallDisplayIcon={app}\kse.ico
UninstallFilesDir={app}\uninstall
VersionInfoCompany={#SetupCompany}
VersionInfoProductVersion={#AppFullVersion}
VersionInfoVersion={#AppFullVersion}
WizardImageFile={#WizardLeftImageFilename}
WizardSmallImageFile={#WizardTopImageFilename}
WizardStyle=modern
WizardSizePercent=100

[Languages]
Name: english; MessagesFile: "compiler:Default.isl,Messages-en.isl"; LicenseFile: "License-en.rtf"; InfoBeforeFile: "Readme-en.rtf"

[InstallDelete]
Type: files; Name: "{app}\lib\*.jar"
Type: files; Name: "{app}\licenses\*"

[Files]
Source: "{#SourceDir}\*"; DestDir: "{app}"; Flags: recursesubdirs
Source: "icons\*"; DestDir: "{app}"

[Icons]
Name: "{group}\{#AppName}"; Filename: "{app}\{#ExeName}"; IconFilename: "{app}\kse.ico"; Comment: "{cm:IconsAppComment}"; WorkingDir: "{app}"
Name: "{autodesktop}\{#AppName}"; Filename: "{app}\{#ExeName}"; IconFilename: "{app}\kse.ico"; Comment: "{cm:IconsAppComment}"; WorkingDir: "{app}"; Tasks: desktopicon
Name: "{group}\{cm:IconsLicensesName}"; Filename: "{app}\Licenses"; IconFilename: "{app}\licenses.ico"; Comment: "{cm:IconsLicensesComment}"
Name: "{group}\{cm:IconsWebsiteName}"; Filename: "{#AppURL}"; IconFilename: "{app}\www.ico"; Comment: "{cm:IconsWebsiteComment}"

[Tasks]
Name: registerapp; Description: "{cm:TasksRegisterAppDescription}"
Name: desktopicon; Description: "{cm:CreateDesktopIcon}"; Flags: unchecked

[Registry]
;------------------------------------------------------------------------------
; HKA\Software\Microsoft\Windows\CurrentVersion\App Paths\exe_name
; See https://docs.microsoft.com/en-us/windows/win32/shell/app-registration#using-the-app-paths-subkey
;------------------------------------------------------------------------------
Tasks: registerapp; Root: HKA; Subkey: "Software\Microsoft\Windows\CurrentVersion\App Paths\{#ExeName}"; Flags: uninsdeletekeyifempty
Tasks: registerapp; Root: HKA; Subkey: "Software\Microsoft\Windows\CurrentVersion\App Paths\{#ExeName}"; ValueType: string; ValueName: ""; ValueData: "{app}\{#ExeName}"; Flags: uninsdeletevalue
Tasks: registerapp; Root: HKA; Subkey: "Software\Microsoft\Windows\CurrentVersion\App Paths\{#ExeName}"; ValueType: string; ValueName: "Path"; ValueData: "{app}"; Flags: uninsdeletevalue
;------------------------------------------------------------------------------
; HKA\Software\Classes\Applications\exe_name
; HKA\Software\Classes\Applications\exe_name\SupportedTypes
; See https://docs.microsoft.com/en-us/windows/win32/shell/app-registration#using-the-applications-subkey
;------------------------------------------------------------------------------
Tasks: registerapp; Root: HKA; Subkey: "Software\Classes\Applications\{#ExeName}"; Flags: uninsdeletekeyifempty
Tasks: registerapp; Root: HKA; Subkey: "Software\Classes\Applications\{#ExeName}\SupportedTypes"; Flags: uninsdeletekeyifempty
Tasks: registerapp; Root: HKA; Subkey: "Software\Classes\Applications\{#ExeName}"; ValueType: string; ValueName: "DefaultIcon"; ValueData: "{app}\kse.ico"; Flags: uninsdeletevalue
Tasks: registerapp; Root: HKA; Subkey: "Software\Classes\Applications\{#ExeName}"; ValueType: string; ValueName: "FriendlyAppName"; ValueData: "{#AppName}"; Flags: uninsdeletevalue
#sub RegisterSupportedType
  Tasks: registerapp; Root: HKA; Subkey: "Software\Classes\Applications\{#ExeName}\SupportedTypes"; ValueType: string; ValueName: "{#Extensions[i]}"; ValueData: ""; Flags: uninsdeletevalue
#endsub
#for { i = 0; i < NumExtensions; i++ } RegisterSupportedType
;------------------------------------------------------------------------------
; HKA\Software\RegisteredApplications
;------------------------------------------------------------------------------
Tasks: registerapp; Root: HKA; Subkey: "Software\RegisteredApplications"; ValueType: string; ValueName: "{#AppName}"; ValueData: "Software\{#AppPublisher}\{#AppName}\Capabilities"; Flags: uninsdeletevalue
;------------------------------------------------------------------------------
; HKA\Software\app_publisher\app_name
; HKA\Software\app_publisher\app_name\Capabilities
; HKA\Software\app_publisher\app_name\Capabilities\FileAssociations
; See https://docs.microsoft.com/en-us/windows/win32/shell/default-programs#registeredapplications
;------------------------------------------------------------------------------
Tasks: registerapp; Root: HKA; Subkey: "Software\{#AppPublisher}"; Flags: uninsdeletekeyifempty
Tasks: registerapp; Root: HKA; Subkey: "Software\{#AppPublisher}\{#AppName}"; Flags: uninsdeletekey
Tasks: registerapp; Root: HKA; Subkey: "Software\{#AppPublisher}\{#AppName}\Capabilities"; ValueType: string; ValueName: "ApplicationDescription"; ValueData: "{cm:DescriptionApplication}"
Tasks: registerapp; Root: HKA; Subkey: "Software\{#AppPublisher}\{#AppName}\Capabilities"; ValueType: string; ValueName: "ApplicationName"; ValueData: "{#AppName}"
#sub RegisterCapability
  Tasks: registerapp; Root: HKA; Subkey: "Software\{#AppPublisher}\{#AppName}\Capabilities\FileAssociations"; ValueType: string; ValueName: "{#Extensions[i]}"; ValueData: "{#ProgIdName}{#Extensions[i]}"
#endsub
#for { i = 0; i < NumExtensions; i++ } RegisterCapability
;------------------------------------------------------------------------------
; HKA\Software\Classes\.extension
; HKA\Software\Classes\.extension\OpenWithProgIds
; HKA\Software\Classes\app_name.extension
; HKA\Software\Classes\App_name.extension\DefaultIcon
; HKA\Software\Classes\App_name.extension\shell\open\command
; See https://docs.microsoft.com/en-us/windows/win32/shell/fa-file-types
;------------------------------------------------------------------------------
#sub RegisterFileType
  ; Register each extension
  Tasks: registerapp; Root: HKA; Subkey: "Software\classes\{#Extensions[i]}"; Flags: uninsdeletekeyifempty
  Tasks: registerapp; Root: HKA; Subkey: "Software\Classes\{#Extensions[i]}\OpenWithProgIds"; Flags: uninsdeletekeyifempty
  Tasks: registerapp; Root: HKA; Subkey: "Software\Classes\{#Extensions[i]}\OpenWithProgIds"; ValueType: string; ValueName: "{#ProgIdName}{#Extensions[i]}"; ValueData: ""; Flags: uninsdeletevalue
  #define protected FileType ReadIni(AddBackslash(SourcePath) + "appinfo.ini", "File Types", Extensions[i])
  Tasks: registerapp; Root: HKA; Subkey: "Software\Classes\{#ProgIdName}{#Extensions[i]}"; ValueType: string; ValueName: ""; ValueData: "{#FileType}"; Flags: uninsdeletekey
  Tasks: registerapp; Root: HKA; Subkey: "Software\Classes\{#ProgIdName}{#Extensions[i]}\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\keystore.ico"
  Tasks: registerapp; Root: HKA; Subkey: "Software\Classes\{#ProgIdName}{#Extensions[i]}\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\{#ExeName}"" ""%1"""
#endsub
#for { i = 0; i < NumExtensions; i++ } RegisterFileType

[Run]
Filename: "{app}\{#ExeName}"; Description: "{cm:RunLaunchDescription}"; Flags: nowait postinstall skipifsilent unchecked
