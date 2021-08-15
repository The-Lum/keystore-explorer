#preproc ispp

; KeyStore Explorer Setup - Inno Setup messages file

#include AddBackslash(SourcePath) + "includes.iss"

[Messages]
SetupWindowTitle=%1 Setup [{#AppFullVersion}]

[CustomMessages]

; Descriptions
DescriptionApplication={#AppName} is an open source GUI replacement for the Java command-line utilities keytool and jarsigner.
DescriptionJavaKeyStore=Java KeyStore
DescriptionPKCS12KeyStore=PKCS #12 KeyStore
DescriptionJavaBCKeyStore=Java BC KeyStore

; Icons
IconsAppComment=Opens {#AppName}.
IconsLicensesName=Licenses
IconsLicensesComment=Displays the {#AppName} license.
IconsWebsiteName=Visit Web site
IconsWebsiteComment=Opens the {#AppName} web site.

; Tasks
TasksRegisterAppDescription=&Register {#AppName} as file handler for keystore file types

; Run
RunLaunchDescription=&Launch {#AppName}
