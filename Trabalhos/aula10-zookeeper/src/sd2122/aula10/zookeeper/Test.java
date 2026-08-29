package sd2122.aula10.zookeeper;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Test {

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        TestObj t = new TestObj(7, "Hello world!");
        Object[] arg =  {"ODEN", 1};
        System.out.println(t.getClass().getDeclaredMethod("changeMSGNum", String.class, int.class).invoke(t, arg));

    }

}
