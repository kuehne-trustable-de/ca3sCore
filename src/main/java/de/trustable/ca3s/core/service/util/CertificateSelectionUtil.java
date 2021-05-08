package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.repository.PipelineAttributeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
public class CertificateSelectionUtil {

    @Value("#{'${ca3s.certificateSelectionAttributes:}'.split(',')}")
    private List<String> certificateSelectionAttributeList;

    @Autowired
    private PipelineAttributeRepository pipelineAttributeRepository;


    public List<String> getCertificateSelectionAttributes() {

        HashSet<String> selectionAttributeSet = new HashSet<>();

        List<String> araNames = pipelineAttributeRepository.findDistinctByName("RESTR_ARA_%_NAME");

        selectionAttributeSet.addAll(araNames);

        if( certificateSelectionAttributeList.size() > 0 && !certificateSelectionAttributeList.get(0).isEmpty()) {
            selectionAttributeSet.addAll(certificateSelectionAttributeList);
        }

        List<String> sortedList = new ArrayList<>(selectionAttributeSet);
        Collections.sort(sortedList);

        return sortedList;
    }
}
