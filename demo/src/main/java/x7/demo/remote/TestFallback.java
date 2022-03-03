package x7.demo.remote;

import io.xream.x7.reyc.Url;
import org.springframework.stereotype.Service;
import x7.demo.ro.CatFindRo;

@Service
public class TestFallback {

    public void testFallBack(CatFindRo ro){

        /*
         * send to kafka
         */

        System.out.println("fallBack");
    }

    public void test(CatFindRo ro, Url url){

        /*
         * send to kafka
         */

        System.out.println("fallBack with Url");
    }
}
