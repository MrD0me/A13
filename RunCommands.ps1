# RunCommands.ps1
# Utility script to apply the MySQL initialization script inside the T1 database container.

$envFile = "T1-G11/applicazione/manvsclass/.env"
$initScript = "T1-G11/applicazione/manvsclass/mysql_init/init.sql"

if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match "^\s*#") { return }
        if ($_ -match "^\s*([^=]+)=(.*)$") {
            $name = $Matches[1].Trim()
            $value = $Matches[2].Trim().Trim("'\"")
            [System.Environment]::SetEnvironmentVariable($name, $value)
        }
    }
}

$rootPassword = $Env:MYSQL_ROOT_PASSWORD
$database = if ($Env:MYSQL_DATABASE) { $Env:MYSQL_DATABASE } else { "manvsclass" }

if (-not $rootPassword) {
    Write-Error "MYSQL_ROOT_PASSWORD is not set. Please populate $envFile."
    pause
    exit 1
}

if (-not (Test-Path $initScript)) {
    Write-Error "Initialization script not found at $initScript"
    pause
    exit 1
}

Get-Content $initScript | docker exec -i t1-mysql_db mysql -uroot "-p$rootPassword" $database

Write-Host "Initialization script applied to database '$database' on container t1-mysql_db."
pause
