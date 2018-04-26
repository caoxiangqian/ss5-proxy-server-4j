package com.caoxiangqian.ss5.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReqHandler implements Runnable {

    static Logger logger = LoggerFactory.getLogger(ReqHandler.class);

    private Socket socket;
    private Socket remoteSocket;
    private String remoteIp;
    private int remotePort;

    static final int USERNAME_PASSWORD = 2;

    public ReqHandler(Socket socket) {
        super();
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream is = socket.getInputStream();
            byte[] buf = new byte[1024 * 100];
            is.read(buf);
            int ver = buf[0]; // 协议版本
            logger.info("version: {}", ver);
            if (ver != 5) {
                logger.error("not support version {}.", ver);
                closeSocket();
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
                logger.error("not support username_password auth.");
                closeSocket();
                return;
            }

            // 告知client使用哪种方式验证
            OutputStream os = socket.getOutputStream();
            os.write(5);
            os.write(USERNAME_PASSWORD);
            os.flush();
            is.read(buf);
            int userNameLen = buf[1];
            int pwdLen = buf[2 + userNameLen];
            String userName = new String(buf, 2, userNameLen);
            String pwd = new String(buf, 2 + userNameLen + 1, pwdLen);
            logger.info("user: {}, pwd: {}", userName, pwd);
            // @TODO 验证用户名和密码

            // 验证通过后返回给client:1 0
            os.write(new byte[] {1, 0});
            os.flush();
            is.read(buf);
            int ver2 = buf[0]; // 协议版本
            int cmd = buf[1]; // 连接类型
            int rsv = buf[2]; // 保留
            int atyp = buf[3]; // 地址类型 ipV4=1,域名=3,ipV6=4
            remoteIp = ""; // 期望目标ip
            int dstPortIdx = 4;
            logger.info("address type: {}", atyp);
            if (atyp == 1) {
                for (int i = 0; i < 4; i++) {
                    remoteIp += (buf[i + 4] & 0xFF);
                    if (i < 3) {
                        remoteIp += ".";
                    }
                }
                dstPortIdx += 4;
            } else if (atyp == 3) {
                int hostLen = buf[4];
                String host = new String(buf, 5, hostLen);
                logger.info("host: " + host);
                remoteIp = InetAddress.getByName(host).getHostAddress();
                dstPortIdx += (1 + hostLen);
            } else if (atyp == 4) {// ipv6
                for (int i = 0; i < 8; i++) {
                    remoteIp += (((buf[i * 2 + 4] & 0xff) << 8) + (buf[i * 2 + 4 + 1] & 0xff));
                    if(i < 7) {
                        remoteIp += ":";
                    }
                }
            }
            remotePort = ((buf[dstPortIdx] & 0xff) << 8) + (buf[dstPortIdx + 1] & 0xFF); // 期望目标地址
            logger.info("ver: " + ver2 + ", cmd: " + cmd + ", rsv: " + rsv + ",atyp: " + atyp
                    + ", dstAddr: " + remoteIp + ", dstPort: " + remotePort);
            remoteSocket = new Socket(remoteIp, remotePort);
            OutputStream osRemote = remoteSocket.getOutputStream();
            // 05 00 00 01 00 00 00 00 00 00
            os.write(new byte[] {5, 0, 0, 1, 0, 0, 0, 0, 0, 0});
            os.flush();

            CountDownLatch latch = new CountDownLatch(2);
            String msg = remoteIp + ":" + remotePort + "client to remote";
            new Thread(new MyPipe(is, osRemote, latch, msg)).start();
            InputStream isRemote = remoteSocket.getInputStream();
            msg = remoteIp + ":" + remotePort + "remote to client";
            new Thread(new MyPipe(isRemote, os, latch, msg)).start();
            latch.await();
            closeSocket();
            logger.info("one request finished.");
        } catch (Exception e) {
            logger.error(remoteIp + ":" + remotePort + " " + e.getMessage());
            closeSocket();
        }


    }

    private void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                socket = null;
                logger.error(remoteIp + ":" + remotePort + " client socket close failed.", e);
            }
        }
        if (remoteSocket != null) {
            try {
                remoteSocket.close();
            } catch (IOException e) {
                remoteSocket = null;
                logger.error(remoteIp + ":" + remotePort + " remote socket close failed.", e);
            }
        }

    }

}
