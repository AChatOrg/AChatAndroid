package com.hyapp.achat.model.entity

class ProfileMessage(var contact: Contact = Contact()) : Message(type = TYPE_PROFILE)