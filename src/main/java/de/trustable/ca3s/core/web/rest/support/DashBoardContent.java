package de.trustable.ca3s.core.web.rest.support;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.trustable.ca3s.core.repository.CSRRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * {@code GET  /issuedCertificatesByMonth} : .
     *
     * @return the {@link ResponseEntity} .
     */
    @GetMapping("/issuedCertificatesByMonth")
    public ResponseEntity<DataCollection> getIssuedCertificatesByMonth() {

        int nMonth = 12;
        Instant now = Instant.now();
        LocalDateTime ldtNow = LocalDateTime.ofInstant(now, ZoneId.systemDefault());

        int year = ldtNow.getYear();
        int month = ldtNow.getMonthValue();
        HashMap<String, Map<String, Long>> monthDataMap = new HashMap<>();
        for(int i = 0; i < nMonth; i++){
            Map<String, Long> countByType = new HashMap<>();
            countByType.put("INTERNAL", 0L);
            countByType.put("WEB", 0L);
            countByType.put("SCEP", 0L);
            countByType.put("ACME", 0L);
            monthDataMap.put(month + "." + year, countByType);
            LOG.info("counter setup for month {}", month + "." + year);
            month--;
            if(month == 0){
                month = 12;
                year--;
            }
        }

        Instant after = now.minus(365 , ChronoUnit.DAYS);

        List<Object[]> objects = csrRepository.groupIssuedByIssuanceMonth(after);
        LOG.info("objects has #{} elements after {}",objects.size(), after);

        for( Object[] resArr: objects) {
            String summaryMonth = resArr[0].toString();
            String summaryType = resArr[1].toString();
            Long count = (Long) resArr[2];

            LOG.info("resArr: {} / {} / {} of type {}", summaryMonth, summaryType, count, resArr[2].getClass().getName());
            if(monthDataMap.containsKey(summaryMonth)) {
                Map<String, Long> countByTypeMap = monthDataMap.get(summaryMonth);
                LOG.info("month entry for '{}' contains {} pipeline types", summaryMonth, countByTypeMap.size());
 //               for( String type: countByTypeMap.keySet() ){
 //                   LOG.info("type '{}' available, matches '{}' : {}", type, summaryType, type.equals(summaryType));
 //               }
                if(countByTypeMap.containsKey(summaryType)) {
                    Long c = countByTypeMap.get(summaryType);
                    LOG.info("updated: {} / {} to {}", summaryMonth, summaryType, c + count);
                    countByTypeMap.put(summaryType, c + count);
                }else{
                    LOG.info("no counter found for month {} / type {}", summaryMonth, summaryType);
                }
            }else{
                LOG.info("no month entry found for '{}'", resArr[0]);
            }
        }

        DataCollection dc = new DataCollection();
        String[] labels = new String[nMonth];
        dc.setLabels(labels);

        DataSet[] dataSets = new DataSet[1];
        DataSet ds = new DataSet( "issued certificates by month", nMonth );
        dataSets[0] = ds;

        dc.setDatasets(dataSets);

        int i = 0;
        for( String label: monthDataMap.keySet()){
            LOG.info("label: {}", label);
            labels[i] = label;

            Map<String, Long> countByTypeMap = monthDataMap.get(label);
            ds.getData()[i] = 0;
            for( String type: countByTypeMap.keySet() ) {
                ds.getData()[i] += countByTypeMap.get(type).intValue();
            }
            i++;
        }

        return new ResponseEntity<DataCollection>(dc, HttpStatus.OK);
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

        return new ResponseEntity<DataCollection>(dc, HttpStatus.OK);
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
		return new ResponseEntity<DataCollection>(dc, HttpStatus.OK);
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
		return new ResponseEntity<DataCollection>(dc, HttpStatus.OK);
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
		return new ResponseEntity<DataCollection>(dc, HttpStatus.OK);
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
