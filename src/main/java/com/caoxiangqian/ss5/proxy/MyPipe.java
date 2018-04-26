package com.caoxiangqian.ss5.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyPipe implements Runnable {
    static Logger logger = LoggerFactory.getLogger(MyPipe.class);
    private InputStream is;
    private OutputStream os;
    private CountDownLatch latch;
    private String msg;



    @Override
    public void run() {
        byte[] buf = new byte[1024 * 100];
        int len = -1;
        try {
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
                os.flush();
            }
            logger.info("pipe io over");
            latch.countDown();
        } catch (IOException e) {
            logger.error("error: " + msg, e);
            latch.countDown();
        }
    }

    public MyPipe(InputStream is, OutputStream os, CountDownLatch latch,String msg) {
        this.is = is;
        this.os = os;
        this.latch = latch;
        this.msg = msg;
    }

}
