package com.hyapp.achat.model.entity

data class Room(

    var name: String = "",
    var onlineMemberCount: Int = 0,
    var gender: Byte = UserConsts.GENDER_MALE,
    var avatars: MutableList<String> = mutableListOf(),

    var uid: String = "",
    var memberCount: Int = 0

) {
    companion object {

        @JvmStatic
        fun compare(r1: Room, r2: Room): Int {
            if (r1.uid == r2.uid) return 0
            return if (r1.memberCount < r2.memberCount) 1
            else -1
        }
    }
}