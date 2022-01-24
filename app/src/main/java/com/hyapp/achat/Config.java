package com.hyapp.achat;

public interface Config {
    String SERVER_URL = "http://192.168.1.103:24240";

    String SOCKET_QUERY_DATA = "data";

    String OPERATION_LOGIN_GUEST = "loginGuest";
    String OPERATION_RECONNECT_GUEST = "reconnectGuest";

    String ON_DISCONNECT = "disconnect";
    String ON_CONNECT = "connect";

    /*On login*/
    String ON_LOGGED = "logged";
    String ON_USER_CAME = "userCame";
    String ON_USER_LEFT = "userLeft";

    /*On People*/
    String ON_USERS = "users";

    /*On Chat*/
    String ON_PV_MESSAGE = "pvMessage";
    String ON_MESSAGE_SENT = "msgSent";
    String ON_MSG_RECEIVED = "msgReceived";
    String ON_MSG_READ = "msgRead";
    String ON_MSG_READ_RECEIVED = "msgReadReceived";
    String ON_TYPING = "typing";
    String ON_ONLINE_TIME = "onlineTime";
    String ON_ONLINE_TIME_CONTACTS = "onlineTimeContacts";

}
