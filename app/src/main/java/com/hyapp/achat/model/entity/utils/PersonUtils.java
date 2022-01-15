package com.hyapp.achat.model.entity.utils;

import android.util.Pair;

import com.hyapp.achat.R;
import com.hyapp.achat.model.entity.User;

public class PersonUtils {

    public static final int RANK_COLOR_MANAGER = 0xFFAF9200;
    public static final int RANK_COLOR_ADMIN = 0xFF1E8800;
    public static final int RANK_COLOR_SENIOR = 0xFF3C0000;
    public static final int RANK_COLOR_ACTIVE = 0xFF093C00;
    public static final int RANK_COLOR_SPECIAL = 0xFF5C1E00;
    public static final int RANK_COLOR_MEMBER = 0xFF000D3E;
    public static final int RANK_COLOR_GUEST = 0xFF9E9E9E;

    public static Pair<Integer, Integer> rankInt2rankStrResAndColor(byte rank) {
        switch (rank) {
            case User.RANK_GUEST:
                return new Pair<>(R.string.guest, RANK_COLOR_GUEST);
            case User.RANK_MEMBER:
                return new Pair<>(R.string.member, RANK_COLOR_MEMBER);
            case User.RANK_SPECIAL:
                return new Pair<>(R.string.special, RANK_COLOR_SPECIAL);
            case User.RANK_ACTIVE:
                return new Pair<>(R.string.active, RANK_COLOR_ACTIVE);
            case User.RANK_SENIOR:
                return new Pair<>(R.string.senior, RANK_COLOR_SENIOR);
            case User.RANK_ADMIN:
                return new Pair<>(R.string.admin, RANK_COLOR_ADMIN);
            case User.RANK_MANAGER:
                return new Pair<>(R.string.manager, RANK_COLOR_MANAGER);
        }
        return new Pair<>(R.string.guest, RANK_COLOR_GUEST);
    }
}
