package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.NutsResponseParser;

public class DefaultNutsResponseParser implements NutsResponseParser {
    public static final NutsResponseParser INSTANCE=new DefaultNutsResponseParser();
    @Override
    public Object parse(Object response, Class type) {
        if (response == null) {
            return null;
        }
        if (type.isInstance(response)) {
            return response;
        }
        if(type.isEnum()){
            String s = String.valueOf(response).trim();
            if(s.isEmpty()){
                return null;
            }
            try {
                return Enum.valueOf(type, s);
            }catch (Exception ex){
                for (Object enumConstant : type.getEnumConstants()) {
                    if(enumConstant.toString().equalsIgnoreCase(s)){
                        return enumConstant;
                    }
                }
                throw ex;
            }
        }
        switch (type.getName()) {
            case "java.lang.String": {
                return String.valueOf(response);
            }
            case "int":
            case "java.lang.Integer": {
                return Integer.parseInt(String.valueOf(response));
            }
            case "long":
            case "java.lang.Long": {
                return Long.parseLong(String.valueOf(response));
            }
            case "float":
            case "java.lang.Float": {
                return Float.parseFloat(String.valueOf(response));
            }
            case "double":
            case "java.lang.Double": {
                return Double.parseDouble(String.valueOf(response));
            }
            case "boolean":
            case "java.lang.Boolean": {
                if(!(response instanceof String)){
                    response=String.valueOf(response);
                }
                String sReponse = response.toString();
                if(
                        "y".equalsIgnoreCase(sReponse)
                                ||"yes".equalsIgnoreCase(sReponse)
                                ||"t".equalsIgnoreCase(sReponse)
                                ||"true".equalsIgnoreCase(sReponse)
                ){
                    return true;
                }
                if(
                        "n".equalsIgnoreCase(sReponse)
                                ||"no".equalsIgnoreCase(sReponse)
                                ||"f".equalsIgnoreCase(sReponse)
                                ||"false".equalsIgnoreCase(sReponse)
                ){
                    return false;
                }
                throw new IllegalArgumentException("Invalid response "+sReponse);
            }
            default:{
                throw new IllegalArgumentException("Unsupported type "+type.getName());
            }
        }
    }

    @Override
    public Object[] getDefaultAcceptedValues(Class type) {
        if(type.isEnum()){
            return type.getEnumConstants();
        }
        switch (type.getName()) {
            case "java.lang.String": {
                return null;
            }
            case "int":
            case "java.lang.Integer": {
                return null;
            }
            case "long":
            case "java.lang.Long": {
                return null;
            }
            case "float":
            case "java.lang.Float": {
                return null;
            }
            case "double":
            case "java.lang.Double": {
                return null;
            }
            case "boolean":
            case "java.lang.Boolean": {
                return new Object[]{"y","n"};
            }
            default:{
                throw new IllegalArgumentException("Unsupported type "+type.getName());
            }
        }
    }
}
