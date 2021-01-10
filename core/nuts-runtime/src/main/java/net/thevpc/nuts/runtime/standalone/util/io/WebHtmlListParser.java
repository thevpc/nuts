package net.thevpc.nuts.runtime.standalone.util.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WebHtmlListParser {
    public static void main(String[] args) {
//        try(InputStream in=new FileInputStream("/data/spring-repo.htm")){
//            List<String> parse = new WebHtmlListParser().parse(in);
//            if(parse==null){
//                System.out.println("invalid");
//            }else {
//                for (String s : parse) {
//                    System.out.println(s);
//                }
//            }
//        }catch (Exception ex){
//            throw new IllegalArgumentException(ex);
//        }
        try(InputStream in=new FileInputStream("/data/Central Repository.htm")){
            List<String> parse = new WebHtmlListParser().parse(in);
            if(parse==null){
                System.out.println("invalid");
            }else {
                for (String s : parse) {
                    System.out.println(s);
                }
            }
        }catch (Exception ex){
            throw new IllegalArgumentException(ex);
        }
    }

    private enum State{
        EXPECT_DOCTYPE,
        EXPECT_BODY,
        EXPECT_PRE,
        EXPECT_HREF,
    }

    public List<String> parse(InputStream html){
        try {
            List<String> found=new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(html));
            State s=State.EXPECT_DOCTYPE;
            String line ;
            while((line=br.readLine())!=null){
                line=line.trim();
//                System.out.println(s+" : "+line);
                switch (s){
                    case EXPECT_DOCTYPE:{
                        if(!line.isEmpty()){
                            if(line.toLowerCase().startsWith("<!DOCTYPE html".toLowerCase())) {
                                s = State.EXPECT_BODY;
                            }else if(
                                    line.toLowerCase().startsWith("<html>".toLowerCase())
                                    || line.toLowerCase().startsWith("<html ".toLowerCase())
                            ){
                                s=State.EXPECT_BODY;
                            }else{
                                return null;
                            }
                        }
                        break;
                    }
                    case EXPECT_BODY:{
                        if(!line.isEmpty()){
                            if(
                                    line.toLowerCase()
                                    .startsWith("<body>".toLowerCase())
                                    || line.toLowerCase()
                                    .startsWith("<body ".toLowerCase())
                            ){
                                s=State.EXPECT_PRE;
                            }
                        }
                        break;
                    }
                    case EXPECT_PRE:{
                        if(!line.isEmpty()){
                            String lowLine = line;
                            if(
                                    lowLine.toLowerCase()
                                    .startsWith("<pre>".toLowerCase())
                                    || lowLine.toLowerCase()
                                    .startsWith("<pre ".toLowerCase())
                            ){
                                //spring.io
                                if(lowLine.toLowerCase().startsWith("<pre>") && lowLine.toLowerCase().matches("<pre>name[ ]+last modified[ ]+size</pre>(<hr/>)?")){
                                    //just ignore
                                }else if(lowLine.toLowerCase().startsWith("<pre>") && lowLine.toLowerCase().matches("<pre>[ ]*<a href=.*")){
                                    lowLine=lowLine.substring("<pre>".length()).trim();
                                    if(lowLine.toLowerCase().startsWith("<a href=\"")){
                                        int i0 = "<a href=\"".length();
                                        int i1= lowLine.indexOf('\"', i0);
                                       if(i1>0){
                                           found.add(lowLine.substring(i0,i1));
                                           s=State.EXPECT_HREF;
                                       }else{
                                           return null;
                                       }
                                    }
                                }else if(lowLine.toLowerCase().startsWith("<pre ")){
                                    s=State.EXPECT_HREF;
                                }else {
                                    //ignore
                                }
                            }else if(lowLine.toLowerCase().matches("<td .*<strong>last modified</strong>.*</td>")){
                                s=State.EXPECT_HREF;
                            }
                        }
                        break;
                    }
                    case EXPECT_HREF:{
                        if(!line.isEmpty()){
                            String lowLine = line;
                            if(lowLine.toLowerCase().startsWith("</pre>".toLowerCase())){
                                return found;
                            }
                            if(lowLine.toLowerCase().startsWith("</html>".toLowerCase())){
                                return found;
                            }
                            if(lowLine.toLowerCase().startsWith("<a href=\"")){
                                int i0 = "<a href=\"".length();
                                int i1= lowLine.indexOf('\"', i0);
                                if(i1>0){
                                    found.add(lowLine.substring(i0,i1));
                                }else{
                                    //ignore
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }catch (Exception ex){
            System.err.println(ex);
            //ignore
        }
        return null;
    }
}
