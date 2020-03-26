package de.trustable.ca3s.core.web.rest.support;

import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private CertificateRepository certificateRepository;
	
	public static final String RED = "#FF0000";
	public static final String ORANGE = "#FFA500";
	public static final String GRAY = "#888888";
	
	public static final String[] DAY_OF_WEEK = {"So", "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};

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
