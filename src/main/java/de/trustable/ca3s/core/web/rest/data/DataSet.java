package de.trustable.ca3s.core.web.rest.data;


public class DataSet {

	private String label;
	private String[] backgroundColors;
    private int data[];
    
    public DataSet(String label, String color, int dataSize) {
    	this.label = label;
    	setBackgroundColor(color);
    	this.data = new int[dataSize];
    	for( int i = 0; i < dataSize; i++) {
    		this.data[i] = 0;
    	}
    }
    
    public DataSet(String label, String[] colors, int dataSize) {
    	this.label = label;
    	this.backgroundColors = colors;
    	this.data = new int[dataSize];
    	for( int i = 0; i < dataSize; i++) {
    		this.data[i] = 0;
    	}
    }
    
    public DataSet(String label, int dataSize) {
    	this.label = label;
    	this.backgroundColors = new String[dataSize];
    	this.data = new int[dataSize];
    	for( int i = 0; i < dataSize; i++) {
    		this.data[i] = 0;
    		this.backgroundColors[i] = "#000000";
    	}
    }
    
	public String getLabel() {
		return label;
	}
	public String[] getBackgroundColor() {
		return backgroundColors;
	}
	public int[] getData() {
		return data;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public void setBackgroundColor(String backgroundColor) {
    	this.backgroundColors = new String[1];
    	this.backgroundColors[0] = backgroundColor;
	}
	public void setBackgroundColor(String[] backgroundColors) {
		this.backgroundColors = backgroundColors;
	}
	public void setData(int[] data) {
		this.data = data;
	}

    
}
