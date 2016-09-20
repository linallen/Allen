SET PR_PATH=%CD%
SET PR_DESC="AAI Shell Service"
SET PR_SERVICE_NAME=AAI
SET PR_JAR=Server0.0.1.jar
SET START_CLASS=pkgShellService.Server
SET START_METHOD=start
SET START_PARAMS=version_2
SET STOP_CLASS=pkgShellService.Server
SET STOP_METHOD=stop
SET STOP_PARAMS=0
SET JVM_OPTIONS=-Dapp.home=%PR_PATH%

prunsrv //IS//%PR_SERVICE_NAME% --Install="%PR_PATH%\prunsrv.exe" --Description=%PR_DESC% --Jvm=auto --Startup=auto --StartMode=jvm --StartClass=%START_CLASS% --StartMethod=%START_METHOD% ++StartParams=%START_PARAMS% --StopMode=jvm --StopClass=%STOP_CLASS% --StopMethod=%STOP_METHOD% ++StopParams=%STOP_PARAMS% --Classpath="%PR_PATH%\%PR_JAR%" --DisplayName="%PR_SERVICE_NAME%" ++JvmOptions=%JVM_OPTIONS%


prunsrv //TS//%PR_SERVICE_NAME%


sc delete %PR_SERVICE_NAME%
