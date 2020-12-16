package net.thevpc.nuts.ntalk;

public final class NTalkConstants {
    public static final int DEFAULT_PORT =1401;
    public static final int DEFAULT_BACKLOG =50;
    public static final String DEFAULT_ADDRESS ="localhost";
    public static final int CMD_CONNECT = 1;
    public static final int CMD_SERVICE = 2;
    public static final int CMD_RECONNECT = 3;
    public static final int CMD_REQUEST = 11;
    public static final int CMD_RESPONSE_OK = 12;
    public static final int CMD_RESPONSE_KO = 13;
    public static final int CMD_NEW_JOB = 13;
    public static final int CMD_QUIT = 255;

    public static final int OK = 256;
    public static final int OK_JOB = 257;
    public static final int KO = 258;
    public static final int KO_JOB = 259;

    public static final int ERR_SERVICE_NOT_FOUND = -11;
    public static final int ERR_CLIENT_ERROR = -12;

    public static final int ERR_SERVICE_ALREADY_REGISTERED = -21;
    public static final int ERR_SERVER_ERROR = -22;
    public static final int ERR_UNKNOWN = -100;

    private NTalkConstants() {
    }

    public static final String errorCode(int code){
        switch (code){
            case ERR_UNKNOWN:return "UNKNOWN";
            case ERR_SERVICE_NOT_FOUND:return "SERVICE_NOT_FOUND";
            case ERR_SERVER_ERROR:return "SERVER_ERROR";
            case ERR_CLIENT_ERROR:return "CLIENT_ERROR";
            case ERR_SERVICE_ALREADY_REGISTERED:return "SERVICE_ALREADY_REGISTERED";
            default:return "ERROR#"+code;
        }
    }
}
