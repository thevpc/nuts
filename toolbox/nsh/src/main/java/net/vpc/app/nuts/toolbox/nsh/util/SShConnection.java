package net.vpc.app.nuts.toolbox.nsh.util;

import com.jcraft.jsch.*;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.RuntimeIOException;

import java.io.*;
import java.util.Properties;

public class SShConnection implements AutoCloseable{
    private Session session;

    public SShConnection(String user, String host, int port, String keyFilePath, String keyPassword) {
        try {
            JSch jsch = new JSch();

            if (keyFilePath == null && keyPassword==null) {
                keyFilePath=System.getProperty("user.home")+ FileUtils.getNativePath("/.ssh/id_rsa");
            }
            if (keyFilePath != null) {
                if (keyPassword != null) {
                    jsch.addIdentity(keyFilePath, keyPassword);
                } else {
                    jsch.addIdentity(keyFilePath);
                }
            }

            Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            if(port<=0){
                port=22;
            }
            session = jsch.getSession(user, host, port);
            session.setConfig(config);
            session.connect();
        } catch (JSchException e) {
            throw new RuntimeIOException(e.getMessage());
        }
    }

    public static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //         -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }

    public void exec(String command)  {
        try{
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);

            // X Forwarding
            // channel.setXForwarding(true);

            //channel.setInputStream(System.in);
            channel.setInputStream(null);

            //channel.setOutputStream(System.out);

            //FileOutputStream fos=new FileOutputStream("/tmp/stderr");
            //((ChannelExec)channel).setErrStream(fos);
            ((ChannelExec)channel).setErrStream(System.err);

            InputStream in=channel.getInputStream();

            channel.connect();

            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    System.out.print(new String(tmp, 0, i));
                }
                if(channel.isClosed()){
                    if(in.available()>0) continue;
                    System.out.println("exit-status: "+channel.getExitStatus());
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }
            channel.disconnect();
        }catch (IOException ex){
            throw new RuntimeIOException(ex);
        }catch (JSchException ex){
            throw new RuntimeIOException(ex.getMessage());
        }
    }

    public void rm(String from,boolean R )  {
        exec("rm "+(R?"-R":""));
    }

    public void copyRemoteToLocal(String from, String to)  {
        try{
        String prefix = null;

        if (new File(to).isDirectory()) {
            prefix = to + File.separator;
        }

        // exec 'scp -f rfile' remotely
        String command = "scp -f " + from;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        byte[] buf = new byte[1024];

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        while (true) {
            int c = checkAck(in);
            if (c != 'C') {
                break;
            }

            // read '0644 '
            in.read(buf, 0, 5);

            long filesize = 0L;
            while (true) {
                if (in.read(buf, 0, 1) < 0) {
                    // error
                    break;
                }
                if (buf[0] == ' ') break;
                filesize = filesize * 10L + (long) (buf[0] - '0');
            }

            String file = null;
            for (int i = 0; ; i++) {
                in.read(buf, i, 1);
                if (buf[i] == (byte) 0x0a) {
                    file = new String(buf, 0, i);
                    break;
                }
            }

            System.out.println("file-size=" + filesize + ", file=" + file);

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            // read a content of lfile
            FileOutputStream fos = new FileOutputStream(prefix == null ? to : prefix + file);
            int foo;
            while (true) {
                if (buf.length < filesize) foo = buf.length;
                else foo = (int) filesize;
                foo = in.read(buf, 0, foo);
                if (foo < 0) {
                    // error
                    break;
                }
                fos.write(buf, 0, foo);
                filesize -= foo;
                if (filesize == 0L) break;
            }

            if (checkAck(in) != 0) {
                System.exit(0);
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            try {
                if (fos != null) fos.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }

        channel.disconnect();
        }catch (IOException ex){
            throw new RuntimeIOException(ex);
        }catch (JSchException ex){
            throw new RuntimeIOException(ex.getMessage());
        }
    }


    public void copyLocalToRemote(String from, String to) {
        try {
            boolean ptimestamp = true;

            // exec 'scp -t rfile' remotely
            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + to;
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                System.exit(0);
            }

            File _lfile = new File(from);

            if (ptimestamp) {
                command = "T" + (_lfile.lastModified() / 1000) + " 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    System.exit(0);
                }
            }

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = _lfile.length();
            command = "C0644 " + filesize + " ";
            if (from.lastIndexOf('/') > 0) {
                command += from.substring(from.lastIndexOf('/') + 1);
            } else {
                command += from;
            }

            command += "\n";
            out.write(command.getBytes());
            out.flush();

            if (checkAck(in) != 0) {
                System.exit(0);
            }

            // send a content of lfile
            FileInputStream fis = new FileInputStream(from);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); //out.flush();
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            if (checkAck(in) != 0) {
                System.exit(0);
            }
            out.close();

            try {
                if (fis != null) fis.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }

            channel.disconnect();
        }catch (IOException ex){
            throw new RuntimeIOException(ex);
        }catch (JSchException ex){
            throw new RuntimeIOException(ex.getMessage());
        }
    }

    public void close(){
        session.disconnect();
    }

}
