package com.hyapp.achat;

public interface Config {
    String SERVER_URL = "http://192.168.1.103:24240";

    String SOCKET_QUERY_DATA = "data";

    String OPERATION_LOGIN_GUEST = "loginGuest";

    String ON_DISCONNECT = "disconnect";
    String ON_CONNECT = "connect";

    /*On login*/
    String ON_LOGGED = "logged";
    String ON_USER_CAME = "userCame";
    String ON_USER_LEFT = "userLeft";

    /*On People*/
    String ON_PEOPLE = "people";

    /*On Chat*/
    String ON_PV_MESSAGE = "pvMessage";
}
