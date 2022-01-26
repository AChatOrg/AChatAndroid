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
    String ON_LOGOUT = "logout";
    String ON_USER_CAME = "userCame";
    String ON_USER_LEFT = "userLeft";
    String ON_ROOM_CREATE = "roomCreate";
    String ON_ROOM_DELETE = "roomDelete";
    String ON_ROOM_MEMBER_ADDED = "roomMemberAdded";
    String ON_ROOM_MEMBER_REMOVED = "roomMemberRemoved";

    /*On People*/
    String ON_USERS = "users";
    String ON_ROOMS = "rooms";
    String ON_CREATE_ROOM = "createRoom";

    /*On Pv Chat*/
    String ON_MESSAGE = "message";
    String ON_MESSAGE_SENT = "msgSent";
    String ON_MSG_RECEIVED = "msgReceived";
    String ON_MSG_READ = "msgRead";
    String ON_MSG_READ_RECEIVED = "msgReadReceived";
    String ON_TYPING = "typing";
    String ON_ONLINE_TIME = "onlineTime";
    String ON_ONLINE_TIME_CONTACTS = "onlineTimeContacts";
    /*On Pv Chat*/
    String ON_JOIN_LEAVE_ROOM = "joinLeaveRoom";
}
