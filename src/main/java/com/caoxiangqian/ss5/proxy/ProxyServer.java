package com.caoxiangqian.ss5.proxy;

import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServer {
    
    static Logger logger = LoggerFactory.getLogger(ProxyServer.class);
    
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(1080);
        int i = 0;
        while (true) {
            i++;
            Socket socket = ss.accept();
            new Thread(new ReqHandler(socket)).start();
            logger.info("request: {}", i);
        }

    }

}
