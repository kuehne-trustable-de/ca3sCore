package de.trustable.ca3s.core.web.rest;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import de.trustable.ca3s.core.repository.CSRViewRepository;
import de.trustable.ca3s.core.service.dto.CSRView;
import de.trustable.ca3s.core.service.dto.CertificateView;
import io.github.jhipster.web.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
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
 * REST controller for managing {@link de.trustable.ca3s.core.service.dto.CSRView}.
 */
@RestController
@RequestMapping("/api")
public class CSRListResource {

	@Autowired
	CSRViewRepository csrViewRepository;


    private final Logger log = LoggerFactory.getLogger(CSRListResource.class);


    /**
     * {@code GET  /csrLisr} : get all the csrs.
     *

     * @param pageable the pagination information.

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of certificates in body.
     */
    @GetMapping("/csrList")
    public ResponseEntity<List<CSRView>> getAllCsrs(Pageable pageable, HttpServletRequest request) {
        log.debug("REST request to get a page of CSRViews");
        Page<CSRView> page = csrViewRepository.findSelection(request.getParameterMap());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /certificates} : get all the CSRs.
     *
     * @param pageable the pagination information.

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of certificates in body.
     */
    @GetMapping("/csrListCSV")
    public ResponseEntity<String> getAllCsrAsCSV(Pageable pageable, HttpServletRequest request) {
        log.debug("REST request to get a page of CSRView");

        Map<String, String[]> paramMap = new HashMap<>();
        for( String key: request.getParameterMap().keySet()){
            if( key == "offset" || key == "limit"){
                continue;
            }
            paramMap.put(key, request.getParameterMap().get(key));
        }
        paramMap.put("offset", new String[]{"0"});
        paramMap.put("limit", new String[]{"1000"});

        Page<CSRView> page = csrViewRepository.findSelection(paramMap);

        List<CSRView> cvList = new ArrayList<>();
        for( CSRView cv: page.getContent()){
            Optional<CSRView> optionalCSRView = csrViewRepository.findbyCSRId(cv.getId());
            if(optionalCSRView.isPresent()){
                cvList.add(optionalCSRView.get());
                log.debug("returning csr #{}", cv.getId());
            }
        }

        Writer writer = new StringWriter();
        ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
        mappingStrategy.setType(CSRView.class);

        StatefulBeanToCsv<CSRView> beanToCsv = new StatefulBeanToCsvBuilder<CSRView>(writer)
//            .withMappingStrategy(mappingStrategy)
            .withSeparator(';')
            .withApplyQuotesToAll(false)
            .withQuotechar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
            .build();

        try {
            beanToCsv.write(cvList);
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            log.warn("problem building csv response", e);
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().contentType(CertificateListResource.TEXT_CSV_TYPE).body(writer.toString());
    }

}
