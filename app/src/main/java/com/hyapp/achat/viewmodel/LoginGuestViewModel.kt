package com.hyapp.achat.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hyapp.achat.Config
import com.hyapp.achat.model.LoginRepo
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.CurrentUserLive
import com.hyapp.achat.model.entity.Person
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.model.entity.Login
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.preferences.LoginPreferences
import com.hyapp.achat.viewmodel.service.SocketService
import com.hyapp.achat.viewmodel.utils.NetUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginGuestViewModel(application: Application) : AndroidViewModel(application) {

    private val _loggedFlow = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    val loggedFlow = _loggedFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            LoginRepo.loggedState.collect { people ->
                val contact = ContactDao.get(people.key!!.uid)
                        ?: Contact(people, Contact.TIME_ONLINE)
                CurrentUserLive.value = contact
                _loggedFlow.emit(Event(Event.Status.SUCCESS))
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
            val genderByte: Byte = if (gender) Person.GENDER_MALE else Person.GENDER_FEMALE
            LoginPreferences.singleton(context).putLoginGuest(nameTrim, bioTrim, genderByte)
            _loggedFlow.tryEmit(Event(Event.Status.LOADING))
            val loginEventJsonStr = Gson().toJson(Login(Config.OPERATION_LOGIN_GUEST, nameTrim, bioTrim, genderByte))
            SocketService.start(context, loginEventJsonStr)
            LoginPreferences.singleton(context).putLoginEvent(loginEventJsonStr)
        }
    }

    fun cancelLogin() {
        val context = getApplication<Application>().applicationContext
        context.stopService(Intent(context, SocketService::class.java))
    }

    val savedName: String
        get() = LoginPreferences.singleton(getApplication<Application>().applicationContext).name

    val savedBio: String
        get() = LoginPreferences.singleton(getApplication<Application>().applicationContext).bio

    val savedGender: Boolean
        get() {
            val gender = LoginPreferences.singleton(getApplication<Application>().applicationContext).gender
            return gender == Person.GENDER_MALE.toInt()
        }

    val nameHistory: Array<String>
        get() {
            val set = LoginPreferences.singleton(getApplication<Application>().applicationContext).nameSet
            return set.toTypedArray()
        }

    val bioHistory: Array<String>
        get() {
            val set = LoginPreferences.singleton(getApplication<Application>().applicationContext).bioSet
            return set.toTypedArray()
        }
}