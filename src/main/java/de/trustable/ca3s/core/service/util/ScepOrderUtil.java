package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.repository.ScepOrderAttributeRepository;
import de.trustable.ca3s.core.domain.ScepOrder;
import de.trustable.ca3s.core.domain.ScepOrderAttribute;
import de.trustable.ca3s.core.service.dto.CertificateView;
import de.trustable.ca3s.core.service.dto.ScepOrderView;
import de.trustable.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ScepOrderUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ScepOrderUtil.class);

    private final ScepOrderAttributeRepository scepOrderAttributeRepository;

    public ScepOrderUtil(ScepOrderAttributeRepository scepOrderAttributeRepository) {
        this.scepOrderAttributeRepository = scepOrderAttributeRepository;
    }


    public ScepOrderView from(ScepOrder scepOrder){

        ScepOrderView scepOrderView = new ScepOrderView();

        scepOrderView.setId(scepOrder.getId());
        scepOrderView.setTransId(scepOrder.getTransId());

        scepOrderView.setStatus(scepOrder.getStatus());
        scepOrderView.setRealm(scepOrder.getRealm());
        scepOrderView.setPipelineName(scepOrder.getPipeline().getName());
        scepOrderView.setRequestedOn(scepOrder.getRequestedOn());
        scepOrderView.setRequestedBy(scepOrder.getRequestedBy());

        scepOrderView.setAsyncProcessing(scepOrder.getAsyncProcessing());
        scepOrderView.setPasswordAuthentication(scepOrder.getPasswordAuthentication());

        if( scepOrder.getCertificate() != null) {
            scepOrderView.setCertificateId(scepOrder.getCertificate().getId());
            scepOrderView.setSubject(scepOrder.getCertificate().getSubject());
            scepOrderView.setSans(scepOrder.getCertificate().getSans());

            CertificateView certificateView = new CertificateView(scepOrder.getCertificate());
            scepOrderView.setSanArr(certificateView.getSanArr());
        }

        if( scepOrder.getCsr() != null) {
            scepOrderView.setCsrId(scepOrder.getCsr().getId());
        }

        return scepOrderView;
    }


    /**
     * @param order
     * @param name
     * @param value
     */
    public void setOrderAttribute(ScepOrder order, String name, long value) {
        setOrderAttribute(order, name, Long.toString(value));
    }

    /**
     * @param order
     * @param name
     * @param value
     */
    public void setOrderMultiValueAttribute(ScepOrder order, String name, String value) {
        setOrderAttribute(order, name, value, true);
    }

    /**
     * @param order
     * @param name
     * @param value
     */
    public void setOrderAttribute(ScepOrder order, String name, String value) {
        setOrderAttribute(order, name, value, true);
    }

    /**
     * @param order
     * @param name
     * @param value
     * @param multiValue
     */
    public void setOrderAttribute(ScepOrder order, String name, String value, boolean multiValue) {

        if (name == null) {
            LOG.warn("no use to insert attribute with name 'null'", new Exception());
            return;
        }
        if (value == null) {
            value = "";
        }

        value = CryptoUtil.limitLength(value, 250);

        Collection<ScepOrderAttribute> orderAttrList = order.getAttributes();
        for (ScepOrderAttribute orderAttr : orderAttrList) {
            if (name.equals(orderAttr.getName())) {
                if (value.equalsIgnoreCase(orderAttr.getValue())) {
                    // attribute already present, no use in duplication here
                    return;
                } else {
                    if (!multiValue) {
                        orderAttr.setValue(value);
                        return;
                    }
                }
            }
        }

        ScepOrderAttribute cAtt = new ScepOrderAttribute();
        cAtt.setOrder(order);
        cAtt.setName(name);
        cAtt.setValue(value);

        order.getAttributes().add(cAtt);

        scepOrderAttributeRepository.save(cAtt);
    }

}
