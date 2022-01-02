package com.hyapp.achat.model.utils;

import android.util.Pair;

import com.hyapp.achat.R;

public class PersonUtils {

    public static final byte RANK_GUEST = 0;
    public static final byte RANK_MEMBER = 1;
    public static final byte RANK_SPECIAL = 2;
    public static final byte RANK_ACTIVE = 3;
    public static final byte RANK_SENIOR = 4;
    public static final byte RANK_ADMIN = 5;
    public static final byte RANK_MANAGER = 6;

    public static final int RANK_STR_GUEST = R.string.guest;

    public static final int RANK_COLOR_MANAGER = 0xFFAF9200;
    public static final int RANK_COLOR_ADMIN = 0xFF1E8800;
    public static final int RANK_COLOR_SENIOR = 0xFF3C0000;
    public static final int RANK_COLOR_ACTIVE = 0xFF093C00;
    public static final int RANK_COLOR_SPECIAL = 0xFF5C1E00;
    public static final int RANK_COLOR_MEMBER = 0xFF000D3E;
    public static final int RANK_COLOR_GUEST = 0xFF9E9E9E;

    public static final int GENDER_PEOPLE_CIRCLE_MALE_BG_RES = R.drawable.gender_circle_people_male_bg;
    public static final int GENDER_PEOPLE_CIRCLE_FEMALE_BG_RES = R.drawable.gender_circle_people_female_bg;
    public static final int GENDER_PEOPLE_CIRCLE_MIXED_BG_RES = R.drawable.gender_circle_people_mixed_bg;

    public static final int NOTIF_CONTACT_BG_RES_GREY = R.drawable.notif_contact_bg_grey;
    public static final int NOTIF_CONTACT_BG_RES_GREEN = R.drawable.notif_contact_bg_green;

    public static final int LAST_ONLINE_PROFILE_BG_RES_GREY = R.drawable.last_online_profile_bg_grey;
    public static final int LAST_ONLINE_PROFILE_BG_RES_GREEN = R.drawable.last_online_profile_bg_green;
    public static final int LAST_ONLINE_CONTACT_BG_RES_GREY = R.drawable.last_online_contact_bg_grey;
    public static final int LAST_ONLINE_CONTACT_BG_RES_GREEN = R.drawable.last_online_contact_bg_green;
    public static final int LAST_ONLINE_CHAT_BG_RES_GREY = R.drawable.last_online_chat_bg_grey;
    public static final int LAST_ONLINE_CHAT_BG_RES_GREEN = R.drawable.last_online_chat_bg_green;

    public static Pair<Integer, Integer> rankInt2rankStrResAndColor(byte rank) {
        switch (rank) {
            case RANK_GUEST:
                return new Pair<>(R.string.guest, RANK_COLOR_GUEST);
            case RANK_MEMBER:
                return new Pair<>(R.string.member, RANK_COLOR_MEMBER);
            case RANK_SPECIAL:
                return new Pair<>(R.string.special, RANK_COLOR_SPECIAL);
            case RANK_ACTIVE:
                return new Pair<>(R.string.active, RANK_COLOR_ACTIVE);
            case RANK_SENIOR:
                return new Pair<>(R.string.senior, RANK_COLOR_SENIOR);
            case RANK_ADMIN:
                return new Pair<>(R.string.admin, RANK_COLOR_ADMIN);
            case RANK_MANAGER:
                return new Pair<>(R.string.manager, RANK_COLOR_MANAGER);
        }
        return new Pair<>(R.string.guest, RANK_COLOR_GUEST);
    }
}
