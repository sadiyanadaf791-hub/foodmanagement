# Maven downloader and build script for Windows
# This script downloads Apache Maven and builds the Spring Boot project

$mavenVersion = "3.9.6"
$javaHome = "C:\Program Files\Java\jdk-17"
$projectDir = (Get-Location)
$downloadDir = "$env:TEMP\maven-download"
$mavenDir = "$env:APPDATA\.maven"
$mavenZip = "$downloadDir\apache-maven-$mavenVersion-bin.zip"
$mavenBin = "$mavenDir\apache-maven-$mavenVersion\bin\mvn.cmd"

# Check if Maven already exists
if (Test-Path $mavenBin) {
    Write-Host "Maven found at $mavenBin" -ForegroundColor Green
    & $mavenBin clean package -DskipTests
    exit $LASTEXITCODE
}

Write-Host "Downloading Apache Maven $mavenVersion..." -ForegroundColor Yellow

# Create directories
New-Item -ItemType Directory -Force -Path $downloadDir | Out-Null
New-Item -ItemType Directory -Force -Path $mavenDir | Out-Null

# Download Maven
$url = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
try {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $url -OutFile $mavenZip -ErrorAction Stop
    Write-Host "Maven downloaded successfully" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Failed to download Maven. Please install Maven manually or use mvnw." -ForegroundColor Red
    Write-Host "Download from: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    exit 1
}

# Extract Maven
Write-Host "Extracting Maven..." -ForegroundColor Yellow
Expand-Archive -Path $mavenZip -DestinationPath $mavenDir -Force

# Build project
Write-Host "Building Spring Boot project..." -ForegroundColor Yellow
if (Test-Path $mavenBin) {
    $env:JAVA_HOME = $javaHome
    & $mavenBin clean package -DskipTests
    $exitCode = $LASTEXITCODE
    
    if ($exitCode -eq 0) {
        Write-Host "Build successful! JAR file created at: target\foodwaste-1.0.0.jar" -ForegroundColor Green
        Write-Host "Run the application with: java -jar target\foodwaste-1.0.0.jar" -ForegroundColor Cyan
    } else {
        Write-Host "Build failed with exit code: $exitCode" -ForegroundColor Red
    }
    
    exit $exitCode
} else {
    Write-Host "ERROR: Maven extraction failed" -ForegroundColor Red
    exit 1
}
