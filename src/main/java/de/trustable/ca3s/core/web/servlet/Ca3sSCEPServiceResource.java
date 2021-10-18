package de.trustable.ca3s.core.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.repository.PipelineRepository;

@Transactional
@RestController
public class Ca3sSCEPServiceResource {

	private final ScepServletImpl scepServlet;
    private final PipelineRepository pipeRepo;

	private static final Logger LOGGER = LoggerFactory.getLogger(Ca3sSCEPServiceResource.class);

    @Autowired
	public Ca3sSCEPServiceResource(ScepServletImpl scepServlet, PipelineRepository pipeRepo) {
        this.scepServlet = scepServlet;
        this.pipeRepo = pipeRepo;
        LOGGER.info("in Ca3sSCEPServiceResource()");
	}

	@RequestMapping(value = "/scep/{realm}", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody void handleSCEPRequest(@PathVariable("realm") String realm,
            HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		long startTime = System.currentTimeMillis();

		List<Pipeline> pipelineList = pipeRepo.findActiveByTypeUrl(PipelineType.SCEP, realm);
		if( pipelineList.isEmpty() ) {
			LOGGER.info("no matching pipeline for scep request realm {}", realm);
			throw new ServletException("Request URL not found");
		}

		// transfer additional information thru the given servlet implementation into the callbacks
		scepServlet.requestPipeline.set(pipelineList.get(0));

		try {
			scepServlet.service(request, response);
		}finally {
			// make absolutely sure that the pipeline is dis-connected from the current thread an may not be used in subsequent calls!
			scepServlet.requestPipeline.remove();
		}

		LOGGER.info("duration of scep processing " + (System.currentTimeMillis() - startTime));
	}
}
