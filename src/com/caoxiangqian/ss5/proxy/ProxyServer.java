package com.caoxiangqian.ss5.proxy;

import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(1080);
		int i = 0;
		while (true) {
			i++;
			Socket socket = ss.accept();
			new Thread(new ReqHandler(socket)).start();
			System.out.println("req: " + i);
		}

	}

}
