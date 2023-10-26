# Invoke example: .\run-win.ps1 MAPF00.lvl MAPF01.lvl MAPF02.lvl greedy

$global:NUMBER_OF_SOLVED_LEVELS = 0
$global:SOLVING_TIME = 0
$global:mapsRootDir = 'compLevels/'

function Output-To-Logs {
    param ($Level, $Output)
    $found = $output -match ".*Expanded:\s*(?<expanded>.*), #Frontier.*"
    if ($found -eq 'True') { $expanded = $matches['expanded'] }
    $found = $output -match ".*Generated:\s*(?<generated>.*),.*"
    if ($found -eq 'True') { $generated = $matches['generated'] }
    $_ = $output -match "Level solved: (?<solved>.*)[.]"
    $solved = $matches['solved']
    $_ = $output -match "Actions used: (?<actionsNumber>.*)[.]"
    $actionsNumber = $matches['actionsNumber']
    $_ = $output -match "Time to solve: (?<time>.*) seconds."
    $time = $matches['time']
    $global:SOLVING_TIME += $time
    $res = '{0,40} | {1,10} | {2,10} | {3,10} | {4,10} | {5,10} | {6}' -f $Level, $algorithm, $actionsNumber, $time, $expanded, $generated, $solved
    $res | Out-File -Append -FilePath $filePath
    if ($solved -eq 'Yes') {
        Write-Host "Level solved" -ForegroundColor Green
        $global:NUMBER_OF_SOLVED_LEVELS += 1
    } else {
        Write-Host "Level not solved" -ForegroundColor Red
    }
}

function Run-Given-Levels {
    param ($Filenames, $Count)
    for ( $i = 0; $i -lt $Count; $i++ ) {
        Write-Host "------------- Running level $($Filenames[$i]) with algorithm $algorithm -----------------" -ForegroundColor Yellow
        $output = java -jar mavis.jar -l "$($global:mapsRootDir)$($Filenames[$i])" -c "java -Xmx4G searchclient.SearchClient -$($algorithm)" -g -s 30 -t 180 | Out-String
        Output-To-Logs -Level $Filenames[$i] -Output $output
    } 
}

if ($args.count -lt 2) {
    write-host "Execute template: ./run-win.ps1 [{list of levels}] [algorithm]"
} else {
    $algorithm = $args[$args.count - 1]
    $filePath = '.\logs\output-{0}.log' -f $algorithm
    $fileHeader = '{0,40} | {1,10} | {2,10} | {3,10} | {4,10} | {5,10} | {6}' -f 'Level', 'Algorithm', 'Actions', 'Time' ,'Expanded', 'Generated', 'Solved'
    $fileHeader | Out-File -FilePath $filePath
    
    javac searchclient/*.java

    if ($args[0] -eq '*') {
        $filenames = Get-ChildItem -Path ".\$($global:mapsRootDir)"
        $levelsCount = $filenames.count
        Run-Given-Levels -Filenames $filenames -Count $levelsCount
    } else {
        $levelsCount = $args.count - 1
        Run-Given-Levels -Filenames $args -Count $levelsCount
    }

    $summary = 'Solved levels {0}/{1} in {2} seconds' -f  $global:NUMBER_OF_SOLVED_LEVELS, $levelsCount, $global:SOLVING_TIME
    '' | Out-File -Append -FilePath $filePath
    $summary | Out-File -Append -FilePath $filePath
    Remove-Item searchclient/*.class
}

