package net.thevpc.nuts.runtime.standalone.xtra.ps;

import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPsInfo;
import net.thevpc.nuts.util.NStream;

import java.io.StringReader;

public class WindowsPsCsvCaller {
    public NStream<NPsInfo> call(boolean failFast){
        NPath tempPath=null;
        try {
            tempPath = NPath.ofTempFile();
            String cmd = "Get-WmiObject Win32_Process | ForEach-Object { $o=$_.GetOwner(); $user=if($o){$o.User}else{'N/A'}; $mem=Get-WmiObject Win32_ComputerSystem; $state=if ($_.ExecutionState -eq 0) {'Running'} elseif ($_.ExecutionState -eq 2) {'Sleeping'} else {'Suspended'}; $start=if ($_.CreationDate) {$_.CreationDate.Substring(0,12)} else {'N/A'}; New-Object PSObject -Property @{ USER=$user; PID=$_.ProcessId; CPU=([math]::Round(($_.KernelModeTime+$_.UserModeTime)/1e7,2)); MEM=([math]::Round($_.WorkingSetSize/$mem.TotalPhysicalMemory*100,2)); VSZ=[long]($_.VirtualSize/1KB); RSS=[long]($_.WorkingSetSize/1KB); TTY='N/A'; STAT=$state; START=$start; TIME=([math]::Round(($_.KernelModeTime+$_.UserModeTime)/1e7,2)); COMMAND=$_.CommandLine } } | Export-Csv -NoTypeInformation -Encoding UTF8 \"" + tempPath.toString() + "\"";
            NExecCmd u = NExecCmd.of()
                    .setIn(NExecInput.ofNull())
                    .grabErr()
                    .grabOut()
                    .addCommand(
                            "powershell.exe", "-Command",
                            //"Get-WmiObject Win32_Process | ForEach-Object { $o = $_.GetOwner(); $user = if ($o) { $o.User } else { 'N/A' }; $mem = Get-WmiObject Win32_ComputerSystem; $state = if ($_.ExecutionState -eq 0) { 'Running' } elseif ($_.ExecutionState -eq 2) { 'Sleeping' } else { 'Suspended' }; $start = if ($_.CreationDate) { $_.CreationDate.Substring(0, 12) } else { 'N/A' }; New-Object PSObject -Property @{ USER=$user; PID=$_.ProcessId; CPU=([math]::Round(($_.KernelModeTime + $_.UserModeTime)/1e7, 2)); MEM=([math]::Round($_.WorkingSetSize / $mem.TotalPhysicalMemory * 100, 2)); VSZ=[long]($_.VirtualSize / 1KB); RSS=[long]($_.WorkingSetSize / 1KB); TTY='N/A'; STAT=$state; START=$start; TIME=([math]::Round(($_.KernelModeTime + $_.UserModeTime)/1e7, 2)); COMMAND=$_.CommandLine } }"
                            cmd
                            //                                        "$mem=(Get-WmiObject Win32_ComputerSystem).TotalPhysicalMemory; Get-WmiObject Win32_Process|ForEach-Object{ $o=$_.GetOwner();$user=if($o){$o.User}else{'N/A'};$state=if($_.ExecutionState -eq 0){'Running'}elseif($_.ExecutionState -eq 2){'Sleeping'}else{'Suspended'};$start=if($_.CreationDate){$_.CreationDate.Substring(0,12)}else{'N/A'};New-Object PSObject -Property @{USER=$user;PID=$_.ProcessId;CPU=[math]::Round(($_.KernelModeTime+$_.UserModeTime)/1e7,2);MEM=[math]::Round($_.WorkingSetSize/$mem*100,2);VSZ=[long]($_.VirtualSize/1KB);RSS=[long]($_.WorkingSetSize/1KB);TTY='N/A';STAT=$state;START=$start;TIME=[math]::Round(($_.KernelModeTime+$_.UserModeTime)/1e7,2);COMMAND=$_.CommandLine}}|ConvertTo-Csv -NoTypeInformation  | Out-String -Width 1000"
                    )
                    .setFailFast(failFast);
            String grabbedOutString = u.getGrabbedOutString();
            String tempValue = tempPath.isRegularFile() ? tempPath.readString() : "";
//                    NPath.ofTempIdFile("ps-result.txt", NId.API_ID).writeString(connexionString);
            try(StringReader br = new StringReader(tempValue)) {
                return new WindowsPsCsvParser().parse(new StringReader(tempValue));
            }
        } finally {
            if (tempPath != null && tempPath.isRegularFile()) {
                tempPath.delete();
            }
        }
    }
}
