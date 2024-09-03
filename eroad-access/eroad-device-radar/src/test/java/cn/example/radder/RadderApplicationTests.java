package cn.example.radder;


import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RadderApplicationTests {

    @Test
    void contextLoads() {
    }


    @Test
    void test1() {
        String a = "XA/mwraddar/WJKJ/127.0.0.1/127.0.0.1_20210912215500_20210912220000.txt";
        String b = "127.0.0.1";
        String c = a.substring(0, a.indexOf(b) + b.length());
        System.out.println(c);
    }
}
