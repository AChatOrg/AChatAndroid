package com.hyapp.achat.kotlinlearning

object Repository {
    private val _users = mutableListOf(User("Jane", ""), User("John", null), User("Anna", "Doe"))

    val users: List<User>
        get() = _users

    val formattedUserNames: List<String?>
        get() {
            return _users.map { user -> user.formattedName }
        }
}

val User.formattedName: String
    get() {
        return if (lastName != null) {
            if (firstName != null) {
                "$firstName $lastName"
            } else {
                lastName ?: "Unknown"
            }
        } else {
            firstName ?: "Unknown"
        }
    }