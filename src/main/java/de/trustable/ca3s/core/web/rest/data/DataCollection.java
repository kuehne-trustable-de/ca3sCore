package de.trustable.ca3s.core.web.rest.data;

public class DataCollection {
	
	private String[] labels;
	private DataSet[] datasets;
	
	
	public String[] getLabels() {
		return labels;
	}
	public DataSet[] getDatasets() {
		return datasets;
	}
	public void setLabels(String[] labels) {
		this.labels = labels;
	}
	public void setDatasets(DataSet[] datasets) {
		this.datasets = datasets;
	}
	
}
