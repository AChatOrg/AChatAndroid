package com.hyapp.achat.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hyapp.achat.Config
import com.hyapp.achat.model.LoginRepo
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.objectbox.ObjectBox
import com.hyapp.achat.model.objectbox.UserDao
import com.hyapp.achat.viewmodel.service.SocketService
import com.hyapp.achat.viewmodel.utils.NetUtils
import com.hyapp.achat.viewmodel.utils.SecureUtils
import io.objectbox.BoxStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

@ExperimentalCoroutinesApi
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _loggedFlow = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    val loggedFlow = _loggedFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            LoginRepo.loggedState.collect { res ->
                if (res.status == Resource.Status.SUCCESS) {
                    res.data?.let {
                        UserDao.put(it.apply { id = User.CURRENT_USER_ID })
                        UserLive.value = it
                        _loggedFlow.emit(Event(Event.Status.SUCCESS))
                    }
                } else if (res.status == Resource.Status.ERROR) {
                    _loggedFlow.tryEmit(Event(Event.Status.ERROR, res.message))
                }
            }
        }
    }

    fun loginGuest(name: String, bio: String, gender: Boolean) {
        val context = getApplication<Application>().applicationContext

        if (name.isEmpty()) {
            _loggedFlow.tryEmit(Event(Event.Status.ERROR, Event.MSG_EMPTY))

        } else if (!NetUtils.isNetConnected(context)) {
            _loggedFlow.tryEmit(Event(Event.Status.ERROR, Event.MSG_NET))

        } else {
            val nameTrim = name.trim()
            val bioTrim = bio.trim()
            val genderByte: Byte = if (gender) UserConsts.GENDER_MALE else UserConsts.GENDER_FEMALE

//            Preferences.instance().putLoginGuest(nameTrim, bioTrim, genderByte)

            _loggedFlow.tryEmit(Event(Event.Status.LOADING))

            val uid = UUID.randomUUID().toString()

            val json = JSONObject()
            json.put("operation", Config.OPERATION_LOGIN_GUEST)
            json.put("androidId", SecureUtils.androidId(context))
            json.put("uid", uid)
            json.put("name", nameTrim)
            json.put("bio", bioTrim)
            json.put("gender", genderByte)
            val jsonStr = json.toString()

            Preferences.instance().putLoginInfo(jsonStr)
            SocketService.start(context, jsonStr)
        }
    }

    fun cancelLogin() {
        val context = getApplication<Application>().applicationContext
        context.stopService(Intent(context, SocketService::class.java))
    }

    fun loginUser(username: String, password: String) {
        val context = getApplication<Application>().applicationContext

        if (username.isEmpty()) {
            _loggedFlow.tryEmit(Event(Event.Status.ERROR, Event.MSG_EMPTY))

        } else if (!NetUtils.isNetConnected(context)) {
            _loggedFlow.tryEmit(Event(Event.Status.ERROR, Event.MSG_NET))

        } else {
            val usernameTrim = username.trim()

            _loggedFlow.tryEmit(Event(Event.Status.LOADING))

            val json = JSONObject()
            json.put("operation", Config.OPERATION_LOGIN_USER)
            json.put("username", usernameTrim)
            json.put("password", password)
            json.put("token", "")
            val jsonStr = json.toString()

            Preferences.instance().putLoginUser(usernameTrim)
            Preferences.instance().putLoginInfo(jsonStr)

            SocketService.start(context, jsonStr)
        }
    }

    fun getSavedUsername(): String {
        return if (Preferences.instance() != null)
            Preferences.instance().loginUsername
        else ""
    }

    fun getUsernameHistory(): Array<String> {
        return if (Preferences.instance() != null) {
            val set = Preferences.instance().loginUsernameSet
            set.toTypedArray()
        } else emptyArray()
    }

//    val savedName: String
//        get() = Preferences.instance().loginName
//
//    val savedBio: String
//        get() = Preferences.instance().loginBio
//
//    val savedGender: Boolean
//        get() {
//            val gender = Preferences.instance().loginGender
//            return gender == UserConsts.GENDER_MALE.toInt()
//        }
//
//    val nameHistory: Array<String>
//        get() {
//            val set = Preferences.instance().loginNameSet
//            return set.toTypedArray()
//        }
//
//    val bioHistory: Array<String>
//        get() {
//            val set = Preferences.instance().loginBioSet
//            return set.toTypedArray()
//        }
}