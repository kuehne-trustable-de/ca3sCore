package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.RequestProxyConfig;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RemoteRequestProxyConfigView implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String requestProxyUrl;

    private Boolean active;

    private String[] acmeRealmArr;


    public RemoteRequestProxyConfigView(){}

    public RemoteRequestProxyConfigView(final RequestProxyConfig requestProxyConfig ){
        this.setId(requestProxyConfig.getId());
        this.setName(requestProxyConfig.getName());
        this.setActive(requestProxyConfig.isActive());
        this.setRequestProxyUrl(requestProxyConfig.getRequestProxyUrl());

        List<String> realmList = new ArrayList<>();
        for(Pipeline p: requestProxyConfig.getPipelines()){
            if( p.isActive() &&
                PipelineType.ACME.equals(p.getType())){
                realmList.add(p.getUrlPart());
            }
        }
        this.setAcmeRealmArr(realmList.toArray(new String[0]));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequestProxyUrl() {
        return requestProxyUrl;
    }

    public void setRequestProxyUrl(String requestProxyUrl) {
        this.requestProxyUrl = requestProxyUrl;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String[] getAcmeRealmArr() {
        return acmeRealmArr;
    }

    public void setAcmeRealmArr(String[] acmeRealmArr) {
        this.acmeRealmArr = acmeRealmArr;
    }

    @Override
    public String toString() {
        return "RemoteRequestProxyConfigView{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", requestProxyUrl='" + requestProxyUrl + '\'' +
            ", active=" + active +
            ", acmeRealmArr=" + Arrays.toString(acmeRealmArr) +
            '}';
    }
}
