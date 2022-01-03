package com.hyapp.achat.da.socket;

import com.google.gson.GsonBuilder;
import com.hyapp.achat.Config;
import com.hyapp.achat.model.Contact;
import com.hyapp.achat.model.CurrentUserLive;
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.gson.PeopleDeserializer;
import com.hyapp.achat.model.event.Event;
import com.hyapp.achat.model.event.LoggedEvent;

import org.greenrobot.eventbus.EventBus;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class LoginRepo {

    private final Socket socket;

    public LoginRepo(Socket socket) {
        this.socket = socket;
    }

    public void listen() {
        socket.on(Config.ON_LOGGED, onLogged);
    }

    private final Emitter.Listener onLogged = args -> {
        People people = new GsonBuilder()
                .registerTypeAdapter(People.class, new PeopleDeserializer())
                .create().fromJson(args[0].toString(), People.class);
        CurrentUserLive.INSTANCE.postValue(new Contact(people, Contact.TIME_ONLINE));
        EventBus.getDefault().post(new LoggedEvent(Event.Status.SUCCESS, LoggedEvent.ACTION_ME, people));
    };
}
