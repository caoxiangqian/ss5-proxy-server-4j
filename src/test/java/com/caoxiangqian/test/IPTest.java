package com.caoxiangqian.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPTest {
	
	public static void main(String[] args) {
		try {
			InetAddress addr = InetAddress.getByName("www.baidu.com");
			System.out.println(addr.getHostAddress());
			System.out.println(addr.getHostName());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
