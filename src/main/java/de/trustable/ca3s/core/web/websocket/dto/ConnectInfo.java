package de.trustable.ca3s.core.web.websocket.dto;


import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConnectInfo implements Serializable {


	/**
	 *
	 */
	private static final long serialVersionUID = -2138583552067093476L;


	private String ip;

	public ConnectInfo() {

		InetAddress ia;
		try {
			ia = InetAddress.getLocalHost();
			this.ip = ia.getCanonicalHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			this.ip = "unknown";
		}

	}

	public String getIp() {
		return ip;
	}

}
