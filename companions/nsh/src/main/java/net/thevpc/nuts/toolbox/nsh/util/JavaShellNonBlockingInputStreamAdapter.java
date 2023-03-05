package net.thevpc.nuts.toolbox.nsh.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JavaShellNonBlockingInputStreamAdapter extends FilterInputStream implements JavaShellNonBlockingInputStream {
    private boolean hasMoreBytes =true;
    private boolean closed=false;
    private String name;
    public JavaShellNonBlockingInputStreamAdapter(String name,InputStream in) {
        super(in);
        this.name=name;
    }

    @Override
    public int read() throws IOException {
        if(closed){
            return -1;
        }
        if(available()==0 && !hasMoreBytes()){
            return -1;
        }
        int read = super.read();
        if(read<0){
            hasMoreBytes =false;
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        if(available()==0 && !hasMoreBytes()){
            return -1;
        }
        int read = -1;
        try {
            read = super.read(b);
        }catch (IOException ex){
            //will ignore
        }
        if(read<0){
            hasMoreBytes =false;
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if(available()==0 && !hasMoreBytes()){
            return -1;
        }
        int read = -1;
        try {
            read = super.read(b, off, len);
        }catch (IOException ex){
            //will ignore
        }
        if(read<0){
            hasMoreBytes =false;
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        if(available()==0 && !hasMoreBytes()){
            return 0;
        }
        return super.skip(n);
    }

    @Override
    public int available() throws IOException {
        if(closed){
            return -1;
        }
        int available = -1;
        try{
            available=super.available();
        }catch (IOException ex){
            return -1;
        }
        if(available<0){
            if(!closed){
                close();
            }
            return -1;
        }
        if(available==0 && !hasMoreBytes){
            return -1;
        }
        if(closed){
            return -1;
        }

        return available;
    }

    @Override
    public int readNonBlocking(byte[] b, long timeout) throws IOException {
        return readNonBlocking(b,0,b.length,timeout);
    }
    @Override
    public int readNonBlocking(byte[] b, int off, int len, long timeout) throws IOException {
        long now=System.currentTimeMillis();
        long then=now+timeout;
        long  tic=100;
//        int readAll=0;
        while(true){
            if(closed){
                break;
            }
            int available = available();
            if(available <0) {
                hasMoreBytes =false;
                break;
            }else if(available >0){
                return read(b,off,len);
            }else if(!hasMoreBytes()){
                break;
            }
            now=System.currentTimeMillis();
            if(now>then){
                break;
            }
            try {
                Thread.sleep(tic);
            } catch (InterruptedException e) {
                break;
            }
        }
        return 0;
    }

    public void noMoreBytes(){
        hasMoreBytes =false;
    }

    @Override
    public boolean hasMoreBytes() {
        return hasMoreBytes;
    }

    @Override
    public void close() throws IOException {
        super.close();
        hasMoreBytes =false;
        closed=true;
    }
}
