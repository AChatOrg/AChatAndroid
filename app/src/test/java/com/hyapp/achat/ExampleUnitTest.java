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
        System.out.println(B.success() instanceof A);
    }

    static class A{
        public static A success(){
            return new A();
        }
    }

    static class B extends A{

    }

    static class C extends A{

    }
}