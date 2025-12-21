package net.thevpc.nuts.runtime.standalone.xtra.ps;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NPsInfo;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.util.NStream;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class WindowsPs1Caller {
    private NConnectionString connectionString;

    public WindowsPs1Caller(NConnectionString connectionString) {
        this.connectionString = connectionString;
    }

    public NStream<NPsInfo> call(boolean failFast) {
        String script = "Get-WmiObject Win32_Process | ForEach-Object { $o=$_.GetOwner(); $user=if($o){$o.User}else{'N/A'}; $mem=Get-WmiObject Win32_ComputerSystem; $state=if ($_.ExecutionState -eq 0) {'Running'} elseif ($_.ExecutionState -eq 2) {'Sleeping'} else {'Suspended'}; $start=if ($_.CreationDate) {$_.CreationDate.Substring(0,12)} else {'N/A'}; New-Object PSObject -Property @{ USER=$user; PID=$_.ProcessId; CPU=([math]::Round(($_.KernelModeTime+$_.UserModeTime)/1e7,2)); MEM=([math]::Round($_.WorkingSetSize/$mem.TotalPhysicalMemory*100,2)); VSZ=[long]($_.VirtualSize/1KB); RSS=[long]($_.WorkingSetSize/1KB); TTY='N/A'; STAT=$state; START=$start; TIME=([math]::Round(($_.KernelModeTime+$_.UserModeTime)/1e7,2)); COMMAND=$_.CommandLine } }";
        String base64 = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        NExec u = NExec.ofSystem()
                .setIn(NExecInput.ofNull())
                .at(connectionString)
                .grabErr()
                .grabOut()
                .addCommand("powershell", "-NoProfile", "-EncodedCommand", base64)
                .setFailFast(failFast);
        byte[] resultBytes = u.getGrabbedOutBytes();
        StringReader br = new StringReader(new String(resultBytes, StandardCharsets.UTF_8));
        return new WindowsPs1Parser().parse(br);
    }
}
