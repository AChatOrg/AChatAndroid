package com.hyapp.achat;

import com.hyapp.achat.model.Key;
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.SortedList;

import org.junit.Test;

import java.util.List;
import java.util.Random;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        List<People> list = new SortedList<>(People::compare);
        int lll =0;
        for (int i = 0; i < 1000; i++) {
            String s = i + "";
            list.add(new People(s, s, (byte) 1, null, new Key(s, (byte) rand(), rand(), rand())));
            lll+=Math.log10(i+1);
        }

        System.out.println(SortedList.iii);
        String s = "s";
        list.get(534);
//        list.add(new People(s, s, (byte) 1, null, new Key(s, (byte) rand(), rand(), rand())));
        System.out.println(SortedList.iii);
        System.out.println(lll);

//        for (People people : list) {
//            Key key = people.getKey();
//            System.out.println(people.getName()+ " " + key.getRank() + " " + key.getScore() + " " + key.getLoginTime());
//        }
    }

    public int rand() {
        return new Random().nextInt(100 - 1) + 1;
    }
}