# Download and extract Java 21
Write-Host "Creating directory..."
New-Item -ItemType Directory -Force -Path "C:\Java" | Out-Null

Write-Host "Downloading Java 21 (this may take a few minutes)..."
$ProgressPreference = 'SilentlyContinue'
Invoke-WebRequest -Uri "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jdk_x64_windows_hotspot_21.0.5_11.zip" -OutFile "C:\Java\jdk21.zip"

Write-Host "Extracting..."
Expand-Archive -Path "C:\Java\jdk21.zip" -DestinationPath "C:\Java" -Force

Write-Host "Done! Java 21 installed to C:\Java\jdk-21.0.5+11"
Write-Host ""
Write-Host "Run these commands to use it:"
Write-Host '  set JAVA_HOME=C:\Java\jdk-21.0.5+11'
Write-Host '  set PATH=%JAVA_HOME%\bin;%PATH%'
