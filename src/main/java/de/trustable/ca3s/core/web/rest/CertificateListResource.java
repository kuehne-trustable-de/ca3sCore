package de.trustable.ca3s.core.web.rest;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import de.trustable.ca3s.core.repository.CertificateViewRepository;
import de.trustable.ca3s.core.service.dto.CertificateView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.web.util.PaginationUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.Certificate}.
 */
@RestController
@RequestMapping("/publicapi")
public class CertificateListResource {

    public final static MediaType TEXT_CSV_TYPE = new MediaType("text", "csv");


    final private CertificateViewRepository certificateViewRepository;
    final private int maxCSVRows;


    private final Logger log = LoggerFactory.getLogger(CertificateListResource.class);

    public CertificateListResource(CertificateViewRepository certificateViewRepository,
                                   @Value("${ca3s.ui.download.rows.max:1000}") int maxCSVRows) {
        this.certificateViewRepository = certificateViewRepository;
        this.maxCSVRows = maxCSVRows;
    }


    /**
     * {@code GET  /certificates} : get all the certificates.
     *

     * @param pageable the pagination information.

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of certificates in body.
     */
    @GetMapping("/certificateList")
    @Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
    public ResponseEntity<List<CertificateView>> getAllCertificates(Pageable pageable, HttpServletRequest request) {
        log.debug("REST request to get a page of CertificateViews");

        Page<CertificateView> page = certificateViewRepository.findSelection(request.getParameterMap());
        List<CertificateView> cvList = getFullCertificateViews(page);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return ResponseEntity.ok().headers(headers).body(cvList);
    }

    /**
     * {@code GET  /certificates} : get all the certificates.
     *
     * @param pageable the pagination information.

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of certificates in body.
     */
    @GetMapping("/certificateListCSV")
    @Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
    public ResponseEntity<String> getAllCertificatesAsCSV(Pageable pageable, HttpServletRequest request) {
        log.debug("REST request to get the CSV list of all CertificateViews");

        Map<String, String[]> paramMap = new HashMap<>();
        for( String key: request.getParameterMap().keySet()){
            if(Objects.equals(key, "offset") || Objects.equals(key, "limit")){
                continue;
            }
            paramMap.put(key, request.getParameterMap().get(key));
        }
        paramMap.put("offset", new String[]{"0"});
        paramMap.put("limit", new String[]{"" + maxCSVRows});

        Page<CertificateView> page = certificateViewRepository.findSelection(paramMap);

        List<CertificateView> cvList = getFullCertificateViews(page);

        Writer writer = new StringWriter();
        ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
        mappingStrategy.setType(CertificateView.class);

        StatefulBeanToCsv<CertificateView> beanToCsv = new StatefulBeanToCsvBuilder<CertificateView>(writer)
//            .withMappingStrategy(mappingStrategy)
            . withSeparator(';')
            .withQuotechar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
            .build();

        try {
            beanToCsv.write(cvList);
        } catch (CsvDataTypeMismatchException  | CsvRequiredFieldEmptyException e) {
            log.warn("problem building csv response", e);
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().contentType(TEXT_CSV_TYPE).body(writer.toString());
    }

    public List<CertificateView> getFullCVList(Map<String, String[]> parameterMap){
        Page<CertificateView> page = certificateViewRepository.findSelection(parameterMap);
        return getFullCertificateViews(page);
    }

    @NotNull
    private List<CertificateView> getFullCertificateViews(Page<CertificateView> page) {
        List<CertificateView> cvList = new ArrayList<>();
        for( CertificateView cv: page.getContent()){

            Optional<CertificateView> optionalCertificateView = certificateViewRepository.findbyCertificateId(cv.getId());
            if(optionalCertificateView.isPresent()){
                cvList.add(optionalCertificateView.get());
                log.debug("returning full certificate view  #{}", cv.getId());
            }
        }
        return cvList;
    }
}
