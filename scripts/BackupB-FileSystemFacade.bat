@echo off
set sevenzip=C:\Program Files\7-Zip\7z.exe
set projectname=FileSystemFacade_L
set dirtozip=C:\Philip\Development\Java\FileSystemFacade
set outputdir=B:\Daily\CopyOrZip


FOR /f %%a in ('WMIC OS GET LocalDateTime ^| find "."') DO set wmic_time=%%a
set timestamp=%wmic_time:~0,4%_%wmic_time:~4,2%_%wmic_time:~6,2%__%wmic_time:~8,2%_%wmic_time:~10,2%


set filename=%projectname%__%timestamp%.7z
echo filename is '%filename%'
echo 7zip is '%sevenzip%'
set command="%sevenzip%" a "%outputdir%\%filename%" "%dirtozip%" "-xr!*.class" "-xr!*.dex" "-xr!*.bin"
echo command is '%command%'
%command%
pause