package com.caoxiangqian.ss5.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.print.attribute.standard.OutputDeviceAssigned;

public class ReqHandler implements Runnable {

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
			for(int i = 0; i < len; i++) {
				System.out.print(buf[i] + "\t");
			}
			System.out.println();
			if(len == -1 || len < 2) {
				socket.close();
			}
			int version = buf[0]; // 协议版本
			if(version != 5) {
				socket.close();
			}
			int authNum = buf[1]; // 客户端支持的验证方式的数量
			boolean flag = false;
			for(int i = 2;i < 2 + authNum; i++) {
				if(buf[i] == USERNAME_PASSWORD) {
					flag = true;
					break;
				}
			}
			if(!flag) {
				socket.close();
			}
			OutputStream os = socket.getOutputStream();
			os.write(5);
			os.write(2);
			os.flush();
			len = is.read(buf);
			for(int i = 0; i < len; i++) {
				System.out.print(buf[i] + "\t");
			}
			System.out.println();
			System.out.println(buf[0]);
			int userNameLen = buf[1];
			System.out.println("usernameLen: " + userNameLen);
			System.out.println("username: " + new String(buf, 2, userNameLen));
			int pwdLen = buf[2 + userNameLen];
			System.out.println("pwdLen: " + pwdLen);
			System.out.println("pwd: " + new String(buf, 2 + userNameLen + 1, pwdLen));
			
			os.write(1);
			os.write(0);
			os.flush();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

}
