package de.trustable.ca3s.core.service.schedule;

public class ImportInfo {

	int nImported = 0;
	int nRejected = 0;
	
	
	public int getImported() {
		return nImported;
	}
	public int getRejected() {
		return nRejected;
	}
	public void setImported(int nImported) {
		this.nImported = nImported;
	}
	public void setRejected(int nRejected) {
		this.nRejected = nRejected;
	}

	public void incImported() {
		this.nImported++;
	}
	public void incRejected() {
		this.nRejected++;
	}

	
}
