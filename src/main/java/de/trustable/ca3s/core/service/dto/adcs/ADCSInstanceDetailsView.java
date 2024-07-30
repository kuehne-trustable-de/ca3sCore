package de.trustable.ca3s.core.service.dto.adcs;

import de.trustable.ca3s.adcsCertUtil.ADCSInstanceDetails;

import java.io.Serializable;

public class ADCSInstanceDetailsView implements Serializable {


    private String caName = null;

    private String caType = null;

    private String[] templates = null;


    public ADCSInstanceDetailsView(ADCSInstanceDetails adcsInstanceDetails){

        this.caName = adcsInstanceDetails.getCaName();
        this.caType = adcsInstanceDetails.getCaType();
        this.templates = adcsInstanceDetails.getTemplates();
    }


    public String getCaName() {
        return caName;
    }


    public String getCaType() {
        return caType;
    }


    public String[] getTemplates() {
        return templates;
    }
}
