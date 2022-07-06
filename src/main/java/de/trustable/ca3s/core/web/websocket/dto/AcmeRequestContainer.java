package de.trustable.ca3s.core.web.websocket.dto;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AcmeRequestContainer implements Serializable {

    /**
	 *
	 */
	private static final long serialVersionUID = -3440167696250676840L;


	private final String path;

    private final String realm;

    private String requestBody;

    private final Map<String, String> pathValues = new HashMap<>();

	public AcmeRequestContainer() {

		path = "unknown";
		realm = "unknown";
	} // keep JAXB happy

	public AcmeRequestContainer(String path, String realm) {
		this.path = path;
		this.realm = realm;
	}

	public AcmeRequestContainer(String path, String realm, String requestBody) {
		this(path, realm);
		this.requestBody = requestBody;
	}

	public void addPathVariable(String key, long value) {
		pathValues.put(key, "" + value);
	}

	public void addPathVariable(String key, String value) {
		pathValues.put(key, value);
	}

	public String getPath() {
		return path;
	}

	public String getRealm() {
		return realm;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public Map<String, String> getPathValues() {
		return pathValues;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}


}
