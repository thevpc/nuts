/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class DateCommand extends SimpleNshBuiltin {

    public DateCommand() {
        super("date", DEFAULT_SUPPORT);
    }

    private static class Options {
        //other date
        String date;
        boolean debug;
        String file;
        String iso;
        boolean rfcMail;
        String rfc8601;
        String rfc3339;
        String reference;
        String setdate;
        boolean utc;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected void initCommandLine(NutsCommandLine commandLine) {
        super.initCommandLine(commandLine);
        for (String s : new String[]{
                "-Id","-Idate",
                "-Ih","-Ihours",
                "-Im","-Iminutes",
                "-Is","-Iseconds",
                "-Ins"
        }) {
            commandLine.registerSpecialSimpleOption(s);
        }
    }

    @Override
    protected boolean configureFirst(NutsCommandLine cmdLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = cmdLine.peek();
        switch (a.getStringKey()) {
            case "-d":
            case "--date": {
                if(context.getShell().getOptions().isNsh()){
                    a = cmdLine.nextString();
                    if(a.isEnabled()){
                        options.date=a.getStringValue();
                    }
                }else {
                    a = cmdLine.next();
                    options.date=a.getStringValue();
                }
                return true;
            }
            case "-f":
            case "--file": {
                if(context.getShell().getOptions().isNsh()){
                    a = cmdLine.nextString();
                    if(a.isEnabled()){
                        options.file=a.getStringValue();
                    }
                }else {
                    a = cmdLine.next();
                    options.file=a.getStringValue();
                }
                return true;
            }
            case "--rfc-3339": {
                if(context.getShell().getOptions().isNsh()){
                    a = cmdLine.next();
                    if(a.isEnabled()){
                        String s = a.getStringValue();
                        if(s==null){
                            s="";
                        }
                        options.rfc3339=s;
                    }
                }else {
                    a = cmdLine.next();
                    String s = a.getStringValue();
                    if(s==null){
                        s="";
                    }
                    options.rfc3339=s;
                }
                return true;
            }
            case "--iso-8601": {
                if(context.getShell().getOptions().isNsh()){
                    a = cmdLine.next();
                    if(a.isEnabled()){
                        String s = a.getStringValue();
                        if(s==null){
                            s="";
                        }
                        options.rfc8601=s;
                    }
                }else {
                    a = cmdLine.next();
                    String s = a.getStringValue();
                    if(s==null){
                        s="";
                    }
                    options.rfc8601=s;
                }
                return true;
            }
            case "-s":
            case "--set":
                {
                if(context.getShell().getOptions().isNsh()){
                    a = cmdLine.next();
                    if(a.isEnabled()){
                        String s = a.getStringValue();
                        if(s==null){
                            s="";
                        }
                        options.setdate=s;
                    }
                }else {
                    a = cmdLine.next();
                    String s = a.getStringValue();
                    if(s==null){
                        s="";
                    }
                    options.setdate=s;
                }
                return true;
            }
            case "-Id":
            case "-Idate":
            case "-Ih":
            case "-Ihours":
            case "-Im":
            case "-Iminutes":
            case "-Is":
            case "-Iseconds":
            case "-Ins":
                {
                if(context.getShell().getOptions().isNsh()){
                    a = cmdLine.next();
                    if(a.isEnabled()){
                        options.rfc8601=a.getString().substring(2);
                    }
                }else {
                    a = cmdLine.next();
                    options.rfc8601=a.getString().substring(2);
                }
                return true;
            }
            case "--debug": {
                if(context.getShell().getOptions().isNsh()){
                    a = cmdLine.nextBoolean();
                    if(a.isEnabled()){
                        options.debug=a.getBooleanValue();
                    }
                }else {
                    a = cmdLine.next();
                    options.debug=true;
                }
                return true;
            }
            case "-u":
            case "--utc":
            case "--universal":
                {
                if(context.getShell().getOptions().isNsh()){
                    a = cmdLine.nextBoolean();
                    if(a.isEnabled()){
                        options.utc=a.getBooleanValue();
                    }
                }else {
                    a = cmdLine.next();
                    options.utc=true;
                }
                return true;
            }
            case "-R":
            case "--rfc-email":{
                if(context.getShell().getOptions().isNsh()){
                    a = cmdLine.nextBoolean();
                    if(a.isEnabled()){
                        options.rfcMail=a.getBooleanValue();
                    }
                }else {
                    a = cmdLine.next();
                    options.rfcMail=true;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine cmdLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        ZonedDateTime dateTimeInMyZone = ZonedDateTime.
                of(LocalDateTime.now(), ZoneId.systemDefault());
        if(options.utc) {
            dateTimeInMyZone = dateTimeInMyZone.withZoneSameInstant(ZoneOffset.UTC);
        }
        if(options.iso!=null){
            switch (options.iso){
                case "":
                case "d":
                case "date":{
                    context.setPrintlnOutObject(dateTimeInMyZone.toLocalDate().toString());
                    break;
                }
                case "m":
                case "minutes":{
                    context.setPrintlnOutObject(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm").format(dateTimeInMyZone.toLocalDateTime()));
                    break;
                }
                case "s":
                case "seconds":{
                    context.setPrintlnOutObject(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss").format(dateTimeInMyZone.toLocalDateTime()));
                    break;
                }
                case "ns":{
                    context.setPrintlnOutObject(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSS").format(dateTimeInMyZone.toLocalDateTime()));
                    break;
                }
                default:{
                    //error??
                    context.setPrintlnOutObject(dateTimeInMyZone.toLocalDate().toString());
                    break;
                }
            }
        }else if(options.rfc3339!=null){
            switch (options.rfc3339){
                case "":
                case "d":
                case "date":{
                    context.setPrintlnOutObject(dateTimeInMyZone.toLocalDate().toString());
                    break;
                }
                case "m":
                case "minutes":{
                    context.setPrintlnOutObject(DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mmXXX").format(dateTimeInMyZone.toLocalDate()));
                    break;
                }
                case "s":
                case "seconds":{
                    context.setPrintlnOutObject(DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ssXXX").format(dateTimeInMyZone.toLocalDateTime()));
                    break;
                }
                case "ns":{
                    context.setPrintlnOutObject(DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ss.SSSSSSSSSXXX").format(dateTimeInMyZone.toLocalDateTime()));
                    break;
                }
                default:{
                    //error??
                    context.setPrintlnOutObject(dateTimeInMyZone.toLocalDate().toString());
                    break;
                }
            }
        }else  if(options.rfcMail){
            context.setPrintlnOutObject(DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z").format(dateTimeInMyZone));
        }else{
            context.setPrintlnOutObject(DateTimeFormatter.ofPattern("EEE MMM d hh:mm:ss a Z yyyy").format(dateTimeInMyZone));
        }
    }
}
