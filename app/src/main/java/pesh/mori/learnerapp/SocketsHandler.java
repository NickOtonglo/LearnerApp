package pesh.mori.learnerapp;

/**
 * Created by Nick Otto on 20/05/2019.
 */

public class SocketsHandler {
    static int ARG_SOCKET_REQUIRED_VALUE=0;

    public static boolean validateSockets(int val){
        if (val==ARG_SOCKET_REQUIRED_VALUE){
            return true;
        } else
            return false;
    }
}
