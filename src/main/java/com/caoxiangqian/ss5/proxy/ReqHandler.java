package com.caoxiangqian.ss5.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReqHandler implements Runnable {

    static Logger logger = LoggerFactory.getLogger(ReqHandler.class);

    Socket socket;

    static final int USERNAME_PASSWORD = 2;

    public ReqHandler(Socket socket) {
        super();
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream is = socket.getInputStream();
            byte[] buf = new byte[1024];
            int len = is.read(buf);
            int ver = buf[0]; // 协议版本
            logger.info("version: {}", ver);
            if (ver != 5) {
                logger.info("not support version {}.", ver);
                over();
                return;
            }

            int nMethods = buf[1]; // 客户端支持的验证方式的数量
            boolean flag = false;
            for (int i = 2; i < 2 + nMethods; i++) {
                int method = buf[i]; // 验证方式
                if (method == USERNAME_PASSWORD) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                logger.info("not support username_password auth.");
                over();
                return;
            }

            // 告知client使用哪种方式验证
            OutputStream os = socket.getOutputStream();
            os.write(5);
            os.write(USERNAME_PASSWORD);
            os.flush();
            len = is.read(buf);
            int userNameLen = buf[1];
            int pwdLen = buf[2 + userNameLen];
            String userName = new String(buf, 2, userNameLen);
            String pwd = new String(buf, 2 + userNameLen + 1, pwdLen);
            logger.info("user: {}, pwd: {}", userName, pwd);
            // @TODO 验证用户名和密码

            // 验证通过后返回给client:1 0
            os.write(1);
            os.write(0);
            os.flush();
            is.read(buf);
            int ver2 = buf[0]; // 协议版本
            int cmd = buf[1]; // 连接类型
            int rsv = buf[2]; // 保留
            int atyp = buf[3]; // 地址类型 ipV4=1,域名=3,ipV6=4
            String dstAddr = ""; // 期望目标ip
            int dstPortIdx = 4;
            if(atyp == 1) {
                for(int i = 0;i < 4;i ++) {
                    dstAddr += buf[i + 4] + ".";
                }
                dstPortIdx += 4;
            } else if(atyp == 3) {
                int hostLen = buf[4];
                String host = new String(buf, 5, hostLen);
            } else if(atyp == 4){
                for(int i = 0;i < 8;i ++) {
                    dstAddr += (buf[i]);
                }
                
            }
            int dstPort = buf[dstPortIdx]; // 期望目标地址
            logger.info("ver: " + ver2 + ", cmd: " + cmd + ", rsv: " + rsv + ",atyp: " + atyp + ", dstAddr: " + dstAddr + ", dstPort: " + dstPort);


        } catch (IOException e) {
            e.printStackTrace();
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    socket = null;
                }
            }
        }


    }

    private void over() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e1) {
                socket = null;
            }
        }

    }

}
