package de.trustable.ca3s.core.web.websocket.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AcmeResponseContainer implements Serializable{

    /**
	 *
	 */
	private static final long serialVersionUID = 5210157877375714367L;


	private int status = 500;

    private String content = null;

    private final Map<String, String> headers = new HashMap<>();

    public AcmeResponseContainer() {
    }

    public AcmeResponseContainer(int status) {
    	this.status = status;
    }

    public AcmeResponseContainer(int status, String content) {
    	this(status);
    	this.content = content;
    }

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getContent() {
		return content;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setContent(String content) {
		this.content = content;
	}



}
