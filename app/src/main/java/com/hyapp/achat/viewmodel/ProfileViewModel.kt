package com.hyapp.achat.viewmodel

import android.content.Intent
import androidx.lifecycle.*
import com.hyapp.achat.App
import com.hyapp.achat.Config
import com.hyapp.achat.model.LoginRepo
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.model.UsersRoomsRepo
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.objectbox.MessageDao
import com.hyapp.achat.model.objectbox.UserDao
import com.hyapp.achat.viewmodel.service.SocketService
import com.hyapp.achat.viewmodel.utils.NetUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.json.JSONObject

@ExperimentalCoroutinesApi
class ProfileViewModel(var user: User) : ViewModel() {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val _userInfoLive = MutableLiveData<Resource<UserInfo>>()
    val userInfoLive = _userInfoLive as LiveData<Resource<UserInfo>>

    enum class LikeStatus { ERROR, LIKED, DISLIKED }

    init {
        if (UserLive.value == null) {
            UserDao.get(User.CURRENT_USER_ID)?.let {
                UserLive.value = it
            }
        }
        requestUserInfo()
    }

    fun requestUserInfo() {
        _userInfoLive.value = Resource.loading(null)
        if (!NetUtils.isNetConnected(App.context)) {
            _userInfoLive.value = Resource.error(Event.MSG_NET, null)
        } else {
            viewModelScope.launch {
                UsersRoomsRepo.requestUserInfo(user.uid).collect { pair ->
                    val message = pair.first
                    val userInfo = pair.second
                    when (message) {
                        UsersRoomsRepo.USER_INFO_MSG_SUCCESS -> {
                            _userInfoLive.value = Resource.success(userInfo)
                        }
                        UsersRoomsRepo.USER_INFO_MSG_NOT_FOUND -> {
                            _userInfoLive.value = Resource.error(Event.MSG_EMPTY, null)
                        }
                        else -> {
                            _userInfoLive.value = Resource.error(Event.MSG_ERROR, null)
                        }
                    }
                }
            }
        }
    }

    fun requestLikeUser(): Flow<Pair<LikeStatus, Long>> = callbackFlow {
        if (!NetUtils.isNetConnected(App.context)) {
            trySend(Pair(LikeStatus.ERROR, 0))
        } else {
            viewModelScope.launch {
                UsersRoomsRepo.requestLikeUser(user.uid).collect { pair ->
                    trySend(pair)
                }
            }
        }
        awaitClose()
    }

    fun isCurrUserNotifEnabled(): Boolean {
        return Preferences.instance().isCurrUserNotifEnabled(UserLive.value?.uid ?: "")
    }

    fun setCurrUserNotif(enabled: Boolean) {
        Preferences.instance().setCurrUserNotif(UserLive.value?.uid ?: "", enabled)
    }

    fun setUserNotif(enabled: Boolean) {
        Preferences.instance().setUserNotif(UserLive.value?.uid ?: "", user.uid, enabled)
    }

    fun isUserNotifEnabled(): Boolean {
        return Preferences.instance().isUserNotifEnabled(UserLive.value?.uid ?: "", user.uid)
    }

    fun requestLogout(): Flow<Resource<Boolean>> = callbackFlow {
        if (user.isGuest) {
            trySend(Resource.loading(null))
            if (!NetUtils.isNetConnected(App.context)) {
                trySend(Resource.error(Event.MSG_NET, null))
            } else {
                viewModelScope.launch {
                    LoginRepo.requestLogout().collect { loggedOut ->
                        if (loggedOut) {
                            App.context.stopService(Intent(App.context, SocketService::class.java))
                            withContext(ioDispatcher) {
                                MainViewModel.publicRoomsMessageMap.clear()
                                Preferences.instance().putLogged(false, false)
                                ContactDao.removeALl(user.uid)
                                MessageDao.removeALl(user.uid)
                                UserDao.removeALl(user.uid)
                            }
                            trySend(Resource.success(false/*start login guest activity*/))
                        } else {
                            trySend(Resource.error(Event.MSG_ERROR, null))
                        }
                    }
                }
            }
        } else {
            App.context.stopService(Intent(App.context, SocketService::class.java))
            Preferences.instance().putLogged(false, true)
            trySend(Resource.success(true/*start login user activity*/))
        }
        awaitClose()
    }

    fun requestCheckUsername(username: CharSequence): Flow<Event> = callbackFlow {
        if (username.matches(Regex("^[a-zA-Z_][\\w](?!.*?\\.{2})[\\w.]{1,28}[\\w]\$"))) {
            trySend(Event(Event.Status.LOADING))
            LoginRepo.requestUsernameExist(username).collect { exist ->
                trySend(
                    if (exist) Event(
                        Event.Status.ERROR,
                        Event.MSG_EXIST
                    ) else Event(Event.Status.SUCCESS)
                )
            }
        } else {
            trySend(Event(Event.Status.ERROR, Event.MSG_MATCH))
        }
        awaitClose()
    }

    fun requestRegister(username: String, password: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.loading(null))
        if (!NetUtils.isNetConnected(App.context)) {
            trySend(Resource.error(Event.MSG_NET, null))
        } else {
            LoginRepo.requestRegister(username, password).collect { triple ->
                val user = triple.first
                val token = triple.second
                val refreshToken = triple.third
                if (token.isNotEmpty() && refreshToken.isNotEmpty()) {
                    Preferences.instance().putTokens(token, refreshToken)
                    UserDao.put(user.apply { id = User.CURRENT_USER_ID })
                    UserLive.value = user
                    this@ProfileViewModel.user = user
                    JSONObject(Preferences.instance().loginInfo).let {
                        it.put("operation", Config.OPERATION_RECONNECT_USER)
                        it.put("username", user.username)
                        it.put("password", "")
                        SocketService.ioSocket?.setToken(token)
                        val newJson = it.toString()
                        Preferences.instance().putLoginInfo(newJson)
                        SocketService.ioSocket?.setQuery(newJson)
                    }
                    trySend(Resource.success(user))
                    Preferences.instance().putLoginUser(user.username)
                } else {
                    trySend(Resource.error(Event.MSG_ERROR, null))
                }
            }
        }
        awaitClose()
    }

    class Factory(private var user: User) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(user) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}