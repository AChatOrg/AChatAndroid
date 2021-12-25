package com.hyapp.achat.kotlinlearning

class Doctor : User {
    var str: String? = null

    constructor() : super(firstName = null) {}
    constructor(firstName: String?, lastName: String?, str: String?) : super(firstName, lastName) {
        this.str = str
    }
}