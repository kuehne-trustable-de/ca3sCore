package de.trustable.ca3s.core.web.rest.support;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.trustable.ca3s.core.repository.CSRRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.web.rest.data.DataCollection;
import de.trustable.ca3s.core.web.rest.data.DataSet;

/**
 * REST controller for processing PKCS10 requests and Certificates.
 */
@RestController
@RequestMapping("/publicapi")
public class DashBoardContent {

	private final Logger LOG = LoggerFactory.getLogger(DashBoardContent.class);

    private final CertificateRepository certificateRepository;
    private final CSRRepository csrRepository;

	public static final String RED = "#FF0000";
	public static final String ORANGE = "#FFA500";
	public static final String GRAY = "#888888";

	public static final String[] DAY_OF_WEEK = {"So", "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};

    public DashBoardContent(CertificateRepository certificateRepository, CSRRepository csrRepository) {
        this.certificateRepository = certificateRepository;
        this.csrRepository = csrRepository;
    }

    /**
     * {@code GET  /requestsByMonth} : .
     *                             years: this.requestsYears,
     *                             requestsAcme: this.requestsAcme,
     *                             requestsScep: this.requestsScep,
     *                             requestsWeb: this.requestsWeb
     * @return the {@link ResponseEntity} .
     */
    @GetMapping("/requestsByMonth")
    public ResponseEntity<DataCollection> getRequestsByMonth(@RequestParam(name="years") int years,
                                                             @RequestParam(name="requestsAcme") boolean requestsAcme,
                                                             @RequestParam(name="requestsScep") boolean requestsScep,
                                                             @RequestParam(name="requestsWeb") boolean requestsWeb
                                                             ) {

        int nMonth = 12 * years;
        String[] labels = new String[nMonth];
        Instant now = Instant.now();

        List<RequestType> requestTypeList = new ArrayList<>();
        if( requestsWeb) {
            requestTypeList.add(new RequestType("WEB", "requested Web CSRs", GRAY));
        }
        if( requestsScep) {
            requestTypeList.add(new RequestType("SCEP", "requested SCEP CSRs", RED));
        }
        if( requestsAcme) {
            requestTypeList.add(new RequestType("ACME", "requested ACME CSRs", ORANGE));
        }

        LocalDateTime ldtNow = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
        LocalDateTime ldtYearAgo = ldtNow.minusDays(365L * years);

        int year = ldtYearAgo.getYear();
        int month = ldtYearAgo.getMonthValue();
        HashMap<String, Map<String, Long>> monthDataMap = new HashMap<>();
        for(int i = 0; i < nMonth; i++){
            Map<String, Long> countByType = new HashMap<>();

            countByType.put("INTERNAL", 0L);
            for( RequestType requestType: requestTypeList){
                countByType.put(requestType.getName(), 0L);
            }

            String monthLabel = month + "." + year;
            monthDataMap.put(monthLabel, countByType);
            labels[i] = monthLabel;
            LOG.info("counter setup for month {}", monthLabel);

            month++;
            if(month == 13){
                month = 1;
                year++;
            }
        }

        Instant after = now.minus(years*365 , ChronoUnit.DAYS);

        List<Object[]> objects = csrRepository.groupByTypeRequestedMonth(after);
        LOG.debug("objects has #{} elements after {}",objects.size(), after);

        for( Object[] resArr: objects) {
            String summaryMonth = resArr[0].toString();
            String summaryType = resArr[1].toString();
            Long count = (Long) resArr[2];

            LOG.debug("resArr: {} / {} / {} of type {}", summaryMonth, summaryType, count, resArr[2].getClass().getName());
            if(monthDataMap.containsKey(summaryMonth)) {
                Map<String, Long> countByTypeMap = monthDataMap.get(summaryMonth);
                LOG.debug("month entry for '{}' contains {} pipeline types", summaryMonth, countByTypeMap.size());
 //               for( String type: countByTypeMap.keySet() ){
 //                   LOG.info("type '{}' available, matches '{}' : {}", type, summaryType, type.equals(summaryType));
 //               }
                if(countByTypeMap.containsKey(summaryType)) {
                    Long c = countByTypeMap.get(summaryType);
                    LOG.debug("updated: {} / {} to {}", summaryMonth, summaryType, c + count);
                    countByTypeMap.put(summaryType, c + count);
                }else{
                    LOG.debug("no counter found for month {} / type {}", summaryMonth, summaryType);
                }
            }else{
                LOG.debug("no month entry found for '{}'", resArr[0]);
            }
        }

        DataCollection dc = new DataCollection();
        dc.setLabels(labels);

        DataSet[] dataSets = new DataSet[requestTypeList.size()];
        int j = 0;
        for( RequestType requestType: requestTypeList){
            dataSets[j] = new DataSet( requestType.getLabel(), nMonth );
            dataSets[j].setBackgroundColor(requestType.getColor());
            j++;
        }

        String[][] backgroundColorArr = new String[3][monthDataMap.size()];
        dc.setDatasets(dataSets);

        int i = 0;
        for( String label: monthDataMap.keySet()){
            LOG.debug("label: {}", label);
            Map<String, Long> countByTypeMap = monthDataMap.get(label);

            int k = 0;
            for( RequestType requestType: requestTypeList) {
                dataSets[k].getData()[i] = countByTypeMap.get(requestType.getName()).intValue();
                backgroundColorArr[k][i] = requestType.getColor();
                k++;
            }
            i++;
        }

        for( int ndx = 0; ndx < dataSets.length; ndx++) {
            dataSets[ndx].setBackgroundColor(backgroundColorArr[ndx]);
        }

        return new ResponseEntity<>(dc, HttpStatus.OK);
    }

    /**
     * {@code GET  /expiringCertificatesByDate} : .
     *
     * @return the {@link ResponseEntity} .
     */
    @GetMapping("/expiringCertificatesByDate")
    public ResponseEntity<DataCollection> getExpiringCertificatesByDate() {

        int nDays = 30;
        int urgentLimitDays = 7;
        int urgentWeekendLimitDays = 10;

        Instant after = Instant.now();
        long nowSec = after.getEpochSecond();

        Instant urgent = Instant.now().plus(urgentLimitDays, ChronoUnit.DAYS);
        Instant urgentWeekend = Instant.now().plus(urgentWeekendLimitDays, ChronoUnit.DAYS);

        Instant before = Instant.now().plus(nDays, ChronoUnit.DAYS);
        List<Certificate> certList = certificateRepository.findByValidTo(after, before);

        DataCollection dc = new DataCollection();
        String[] labels = new String[nDays];
        dc.setLabels(labels);

        DataSet[] dataSets = new DataSet[1];
        dc.setDatasets(dataSets);

        for( int i = 0; i < nDays; i++) {
            Instant instant = after.plus(i, ChronoUnit.DAYS);
            int dow = instant.atZone(ZoneId.systemDefault()).getDayOfWeek().getValue();
            labels[i] = DAY_OF_WEEK[dow];
        }

        dataSets[0] = new DataSet( "Expiring soon", nDays );
//		dataSets[1] = new DataSet( "Urgent", RED, nDays );

        for( Certificate cert: certList) {
            if(cert.isRevoked()) {
                LOG.debug("revoked certificate ignored");
            } else {
                int relativeDay = (int)((cert.getValidTo().getEpochSecond() - nowSec) / (3600L * 24L));

                DataSet ds = dataSets[0];

                String color = GRAY;

                if(cert.getValidTo().isBefore(urgent) ) {
                    color = RED;
                }else if(cert.getValidTo().isBefore(urgentWeekend) ) {
                    int dow = cert.getValidTo().atZone(ZoneId.systemDefault()).getDayOfWeek().getValue();
                    if( dow >= 5) {
                        color = RED;
                    }else {
                        color = ORANGE;
                    }
                }

                ds.getBackgroundColor()[relativeDay] = color;
                ds.getData()[relativeDay]++;
            }
        }

        return new ResponseEntity<>(dc, HttpStatus.OK);
    }

    /**
     * {@code GET  /activeCertificatesByHashAlgo} : .
     *
     * @return the {@link ResponseEntity} .
     */
    @GetMapping("/activeCertificatesByHashAlgo")
    public ResponseEntity<DataCollection> getActiveCertificatesByHashAlgo() {

        List<Object[]> algos = certificateRepository.findActiveCertificatesByHashAlgo(Instant.now());

        DataCollection dc = fillDataCollection(algos, "Hash algorithm");
		return new ResponseEntity<>(dc, HttpStatus.OK);
    }

    /**
     * {@code GET  /activeCertificatesByKeyAlgo} : .
     *
     * @return the {@link ResponseEntity} .
     */
    @GetMapping("/activeCertificatesByKeyAlgo")
    public ResponseEntity<DataCollection> getActiveCertificatesByKeyAlgo() {

        List<Object[]> algos = certificateRepository.findActiveCertificatesByKeyAlgo(Instant.now());

        DataCollection dc = fillDataCollection(algos, "Key algorithm");
		return new ResponseEntity<>(dc, HttpStatus.OK);
    }

    /**
     * {@code GET  /activeCertificatesByKeyLength} : .
     *
     * @return the {@link ResponseEntity} .
     */
    @GetMapping("/activeCertificatesByKeyLength")
    public ResponseEntity<DataCollection> getActiveCertificatesByKeyLength() {

        List<Object[]> algos = certificateRepository.findActiveCertificatesByKeyLength(Instant.now());

        DataCollection dc = fillDataCollection(algos, "Key length");
		return new ResponseEntity<>(dc, HttpStatus.OK);
    }

	private DataCollection fillDataCollection(List<Object[]> valuesArr, String headingText) {
		int nAlgos = valuesArr.size();

    	DataCollection dc = new DataCollection();
    	String[] labels = new String[nAlgos];
    	dc.setLabels(labels);
    	DataSet[] dataSets = new DataSet[1];
		DataSet ds = new DataSet( headingText, new String[nAlgos], nAlgos );
		dataSets[0] = ds;

		dc.setDatasets(dataSets);

		int i = 0;
		for( Object[] resArr: valuesArr) {

			if( (resArr.length > 0) && (resArr[0] != null) ) {
				LOG.debug("resArr[0].toString() : {}", resArr[0]);
				labels[i] = resArr[0].toString();
				ds.getData()[i] = ((Long)resArr[1]).intValue();
				ds.getBackgroundColor()[i] = getRandomColor();
			}
			i++;
		}
		return dc;
	}

	static Float MAX_COLOR = Float.valueOf("16777215"); // decimal for 0xfffffff
    String getRandomColor() {

    	BigInteger bi = BigInteger.valueOf((long)(Math.random() * MAX_COLOR));
    	return "#"+ bi.toString(16);
    }
}

class RequestType {
    final private String name;
    final private String label;
    final private String color;


    RequestType(String name, String label, String color) {
        this.name = name;
        this.label = label;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}
