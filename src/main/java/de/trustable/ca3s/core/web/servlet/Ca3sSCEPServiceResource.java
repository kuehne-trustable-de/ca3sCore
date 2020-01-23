package de.trustable.ca3s.core.web.servlet;

import java.io.IOException;

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

@Transactional
@RestController
public class Ca3sSCEPServiceResource {

	@Autowired
	private ScepServletImpl scepServlet;

	private static final Logger LOGGER = LoggerFactory.getLogger(Ca3sSCEPServiceResource.class);

	public Ca3sSCEPServiceResource() {
		LOGGER.info("in Ca3sSCEPServiceResource()");
	}

	@RequestMapping(value = "/ca3sScep/{unit}", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody void handleSCEPRequest(@PathVariable("unit") String unit, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		long startTime = System.currentTimeMillis();

		scepServlet.service(request, response);

		LOGGER.info("duration of scep processing " + (System.currentTimeMillis() - startTime));

	}

}
