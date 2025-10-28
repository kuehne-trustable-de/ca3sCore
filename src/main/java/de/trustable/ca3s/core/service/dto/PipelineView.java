package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;
import java.time.Instant;

import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.Tenant;
import de.trustable.ca3s.core.domain.enumeration.CnAsSanRestriction;
import de.trustable.ca3s.core.domain.enumeration.CsrUsage;
import de.trustable.ca3s.core.domain.enumeration.KeyUniqueness;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;

public class PipelineView implements Serializable {


	/**
	 *
	 */
	private static final long serialVersionUID = 3936438948802709288L;

    private Long id;

    private String name;

    private PipelineType type;

    private String urlPart;

    private String description;

    private int listOrder;

    private Boolean approvalRequired;

    private Boolean active;

    private Instant expiryDate;
    private Long caConnectorId;
    private String caConnectorName;

    private String processInfoNameCreate;
    private String processInfoNameRevoke;
    private String processInfoNameNotify;

    private KeyUniqueness keyUniqueness;
    private CnAsSanRestriction cnAsSanRestriction;

    private RDNRestriction restriction_C;
    private RDNRestriction restriction_CN;
    private RDNRestriction restriction_L;
    private RDNRestriction restriction_O;
    private RDNRestriction restriction_OU;
    private RDNRestriction restriction_S;
    private RDNRestriction restriction_E;

    private RDNRestriction restriction_SAN;

    private RDNRestriction[] rdnRestrictions;

    private ARARestriction[] araRestrictions;

    private boolean tosAgreementRequired;
    private String tosAgreementLink;

    private String website;
    private String[] caaIdentitiyList;
    private String[] selectedCaaIdentitiyList;
    private String eabMode;

    private String[] domainRaOfficerList;

    private Tenant[] allTenantList;
    private Tenant[] selectedTenantList;

    private Authority[] allRolesList;
    private Authority[] selectedRolesList;

    private boolean toPendingOnFailedRestrictions = false;

    private boolean ipAsSubjectAllowed = false;
    private boolean ipAsSANAllowed = false ;

    private AcmeConfigItems acmeConfigItems;

    private SCEPConfigItems scepConfigItems;

    private WebConfigItems webConfigItems;

    private AuditView[] auditViewArr;

    private String[] networkAcceptArr;
    private String[] networkRejectArr;

    private CsrUsage csrUsage = CsrUsage.TLS_SERVER;
    private long[] requestProxyConfigIds = new long[0];


    public PipelineView() {}

	public Long getId() {
		return id;
	}


	public String getName() {
		return name;
	}


	public PipelineType getType() {
		return type;
	}


	public String getUrlPart() {
		return urlPart;
	}


	public String getDescription() {
		return description;
	}


	public Boolean getApprovalRequired() {
		return approvalRequired;
	}

    public Boolean getActive() { return active; }

    public String getCaConnectorName() {
		return caConnectorName;
	}

    public Long getCaConnectorId() {
        return caConnectorId;
    }

    public KeyUniqueness getKeyUniqueness() {
        return keyUniqueness;
    }

    public void setKeyUniqueness(KeyUniqueness keyUniqueness) {
        this.keyUniqueness = keyUniqueness;
    }

    public CnAsSanRestriction getCnAsSanRestriction() {
        return cnAsSanRestriction;
    }
    public void setCnAsSanRestriction(CnAsSanRestriction cnAsSanRestriction) {
        this.cnAsSanRestriction = cnAsSanRestriction;
    }

    public RDNRestriction getRestriction_C() {
		return restriction_C;
	}


	public RDNRestriction getRestriction_CN() {
		return restriction_CN;
	}


	public RDNRestriction getRestriction_L() {
		return restriction_L;
	}


	public RDNRestriction getRestriction_O() {
		return restriction_O;
	}


	public RDNRestriction getRestriction_OU() {
		return restriction_OU;
	}


	public RDNRestriction getRestriction_S() {
		return restriction_S;
	}

    public RDNRestriction getRestriction_E() {
        return restriction_E;
    }

    public void setRestriction_E(RDNRestriction restriction_E) {
        this.restriction_E = restriction_E;
    }

    public boolean isIpAsSubjectAllowed() {
		return ipAsSubjectAllowed;
	}


	public boolean isIpAsSANAllowed() {
		return ipAsSANAllowed;
	}


	public AcmeConfigItems getAcmeConfigItems() {
		return acmeConfigItems;
	}


	public SCEPConfigItems getScepConfigItems() {
		return scepConfigItems;
	}


	public WebConfigItems getWebConfigItems() {
		return webConfigItems;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public void setName(String name) {
		this.name = name;
	}


	public void setType(PipelineType type) {
		this.type = type;
	}


	public void setUrlPart(String urlPart) {
		this.urlPart = urlPart;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public void setApprovalRequired(Boolean approvalRequired) {
		this.approvalRequired = approvalRequired;
	}

    public void setActive(Boolean active) {this.active = active;}

    public void setCaConnectorName(String caConnectorName) {
		this.caConnectorName = caConnectorName;
	}

    public void setCaConnectorId(Long caConnectorId) {
        this.caConnectorId = caConnectorId;
    }

	public void setRestriction_C(RDNRestriction restriction_C) {
		this.restriction_C = restriction_C;
	}


	public void setRestriction_CN(RDNRestriction restriction_CN) {
		this.restriction_CN = restriction_CN;
	}


	public void setRestriction_L(RDNRestriction restriction_L) {
		this.restriction_L = restriction_L;
	}


	public void setRestriction_O(RDNRestriction restriction_O) {
		this.restriction_O = restriction_O;
	}


	public void setRestriction_OU(RDNRestriction restriction_OU) {
		this.restriction_OU = restriction_OU;
	}


	public void setRestriction_S(RDNRestriction restriction_S) {
		this.restriction_S = restriction_S;
	}


	public RDNRestriction getRestriction_SAN() {
		return restriction_SAN;
	}

	public void setRestriction_SAN(RDNRestriction restriction_SAN) {
		this.restriction_SAN = restriction_SAN;
	}

	public void setIpAsSubjectAllowed(boolean ipAsSubjectAllowed) {
		this.ipAsSubjectAllowed = ipAsSubjectAllowed;
	}


	public void setIpAsSANAllowed(boolean ipAsSANAllowed) {
		this.ipAsSANAllowed = ipAsSANAllowed;
	}


	public void setAcmeConfigItems(AcmeConfigItems acmeConfigItems) {
		this.acmeConfigItems = acmeConfigItems;
	}


	public void setScepConfigItems(SCEPConfigItems scepConfigItems) {
		this.scepConfigItems = scepConfigItems;
	}


	public void setWebConfigItems(WebConfigItems webConfigItems) {
		this.webConfigItems = webConfigItems;
	}

	public boolean isToPendingOnFailedRestrictions() {
		return toPendingOnFailedRestrictions;
	}

	public void setToPendingOnFailedRestrictions(boolean toPendingOnFailedRestrictions) {
		this.toPendingOnFailedRestrictions = toPendingOnFailedRestrictions;
	}

	public ARARestriction[] getAraRestrictions() {
		return araRestrictions;
	}

	public void setAraRestrictions(ARARestriction[] araRestrictions) {
		this.araRestrictions = araRestrictions;
	}

	public RDNRestriction[] getRdnRestrictions() {
		return rdnRestrictions;
	}

	public void setRdnRestrictions(RDNRestriction[] rdnRestrictions) {
		this.rdnRestrictions = rdnRestrictions;
	}

    public AuditView[] getAuditViewArr() {
        return auditViewArr;
    }

    public void setAuditViewArr(AuditView[] auditViewArr) {
        this.auditViewArr = auditViewArr;
    }

    public CsrUsage getCsrUsage() {
        return csrUsage;
    }

    public void setCsrUsage(CsrUsage csrUsage) {
        this.csrUsage = csrUsage;
    }

    public int getListOrder() {
        return listOrder;
    }

    public void setListOrder(int listOrder) {
        this.listOrder = listOrder;
    }

    public String[] getDomainRaOfficerList() {
        return domainRaOfficerList;
    }

    public void setDomainRaOfficerList(String[] domainRaOfficerList) {
        this.domainRaOfficerList = domainRaOfficerList;
    }

    public boolean isTosAgreementRequired() {
        return tosAgreementRequired;
    }

    public void setTosAgreementRequired(boolean tosAgreementRequired) {
        this.tosAgreementRequired = tosAgreementRequired;
    }

    public String getTosAgreementLink() {
        return tosAgreementLink;
    }

    public void setTosAgreementLink(String tosAgreementLink) {
        this.tosAgreementLink = tosAgreementLink;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String[] getCaaIdentitiyList() {
        return caaIdentitiyList;
    }

    public void setCaaIdentitiyList(String[] caaIdentitiyList) {
        this.caaIdentitiyList = caaIdentitiyList;
    }

    public String[] getSelectedCaaIdentitiyList() {
        return selectedCaaIdentitiyList;
    }

    public void setSelectedCaaIdentitiyList(String[] selectedCaaIdentitiyList) {
        this.selectedCaaIdentitiyList = selectedCaaIdentitiyList;
    }

    public String[] getNetworkAcceptArr() {
        return networkAcceptArr;
    }

    public void setNetworkAcceptArr(String[] networkAcceptArr) {
        this.networkAcceptArr = networkAcceptArr;
    }

    public String[] getNetworkRejectArr() {
        return networkRejectArr;
    }

    public void setNetworkRejectArr(String[] networkRejectArr) {
        this.networkRejectArr = networkRejectArr;
    }

    public String getEabMode() {
        return eabMode;
    }

    public void setEabMode(String eabMode) {
        this.eabMode = eabMode;
    }

    public long[] getRequestProxyConfigIds() {
        return requestProxyConfigIds;
    }

    public void setRequestProxyConfigIds(long[] requestProxyConfigIds) {
        this.requestProxyConfigIds = requestProxyConfigIds;
    }

    public String getProcessInfoNameCreate() {
        return processInfoNameCreate;
    }

    public void setProcessInfoNameCreate(String processInfoNameCreate) {
        this.processInfoNameCreate = processInfoNameCreate;
    }

    public String getProcessInfoNameRevoke() {
        return processInfoNameRevoke;
    }

    public void setProcessInfoNameRevoke(String processInfoNameRevoke) {
        this.processInfoNameRevoke = processInfoNameRevoke;
    }

    public String getProcessInfoNameNotify() {
        return processInfoNameNotify;
    }

    public void setProcessInfoNameNotify(String processInfoNameNotify) {
        this.processInfoNameNotify = processInfoNameNotify;
    }

    public Tenant[] getAllTenantList() {
        return allTenantList;
    }

    public void setAllTenantList(Tenant[] allTenantList) {
        this.allTenantList = allTenantList;
    }

    public Tenant[] getSelectedTenantList() {
        return selectedTenantList;
    }

    public void setSelectedTenantList(Tenant[] selectedTenantList) {
        this.selectedTenantList = selectedTenantList;
    }

    public Authority[] getAllRolesList() {
        return allRolesList;
    }

    public void setAllRolesList(Authority[] allRolesList) {
        this.allRolesList = allRolesList;
    }

    public Authority[] getSelectedRolesList() {
        return selectedRolesList;
    }

    public void setSelectedRolesList(Authority[] selectedRolesList) {
        this.selectedRolesList = selectedRolesList;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

}
