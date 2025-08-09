package de.trustable.ca3s.core.web.scepservlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.util.PipelineUtil;
import org.jscep.transaction.OperationFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;

@Transactional(dontRollbackOn = OperationFailureException.class)
@RestController
public class Ca3sSCEPServiceResource {

	private final ScepServletImpl scepServlet;
    private final PipelineUtil pipeUtil;

	private static final Logger LOGGER = LoggerFactory.getLogger(Ca3sSCEPServiceResource.class);

    @Autowired
	public Ca3sSCEPServiceResource(ScepServletImpl scepServlet, PipelineUtil pipeUtil) {
        this.scepServlet = scepServlet;
        this.pipeUtil = pipeUtil;
        LOGGER.info("in Ca3sSCEPServiceResource()");
	}

	@RequestMapping(value = "/scep/{realm}", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody void handleSCEPRequest(@PathVariable("realm") String realm,
            HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		long startTime = System.currentTimeMillis();

        Pipeline pipeline = pipeUtil.getPipelineByRealm(PipelineType.SCEP, realm);
        if( pipeline == null ) {
            LOGGER.info("no matching pipeline for scep request realm {}", realm);
        }else if( !pipeline.isActive() ) {
            LOGGER.info("Deactivated pipeline '{}' found for scep request realm '{}'", pipeline.getName(), realm);
            pipeline = null;
        }

        // transfer additional information thru the given servlet implementation into the callbacks
		scepServlet.threadLocalPipeline.set(pipeline);

		try {
			scepServlet.service(request, response);
		}finally {
			// make absolutely sure that the pipeline is dis-connected from the current thread an may not be used in subsequent calls!
			scepServlet.threadLocalPipeline.remove();
		}

		LOGGER.info("duration of scep processing " + (System.currentTimeMillis() - startTime));
	}
}
