package com.hyapp.achat;

public interface Config {
    String SERVER_URL = "https://maroon-jealous-bobcat.glitch.me";
    String SERVER_UPLOAD_AVATAR_URL = SERVER_URL + "/avatar";

    String SOCKET_QUERY_DATA = "data";

    String OPERATION_LOGIN_GUEST = "loginGuest";
    String OPERATION_RECONNECT_GUEST = "reconnectGuest";
    String OPERATION_LOGIN_USER = "loginUser";
    String OPERATION_RECONNECT_USER = "reconnectUser";
    String OPERATION_RECONNECT_USER_BY_REFRESH_TOKEN = "reconnectUserByRefreshToken";

    /*http endpoints*/
    String HTTP_REFRESH_TOKEN = "refreshToken";

    /*On connection err*/
    String CONNECTION_ERR_INCORRECT_PASS = "incorrectPass";
    String CONNECTION_ERR_TOKEN_EXPIRED = "tokenExpired";
    String CONNECTION_ERR_REFRESH_TOKEN_EXPIRED = "refreshTokenExpired";

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
    String ON_ROOM_ONLINE_MEMBER_COUNT = "roomOnlineMemberCount";

    /*On People*/
    String ON_USERS = "users";
    String ON_ROOMS = "rooms";
    String ON_CREATE_ROOM = "createRoom";
    String ON_REQUEST_ROOM_MEMBER_COUNT = "requestRoomMemberCount";
    String ON_REQUEST_USER_INFO = "requestUserInfo";
    String ON_REQUEST_LIKE_USER = "requestLikeUser";
    String ON_REQUEST_EDIT_PROFILE = "requestEditProfile";
    String ON_USER_EDIT = "userEdit";
    String ON_REQUEST_CHECK_USERNAME = "requestCheckUsername";
    String ON_REQUEST_REGISTER = "requestRegister";
    String ON_REQUEST_CHANGE_PASS = "requestChangePass";

    /*On Pv Chat*/
    String ON_MESSAGE = "msg";
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
