package JobPortal.controller;


import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import java.io.IOException;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import JobPortal.model.Company;
import JobPortal.service.CompanyService;
import JobPortal.model.JobOpening;
import JobPortal.service.JobOpeningService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
public class JobOpeningController {

    @Autowired
    private CompanyService companyService;
    
    @Autowired 
    private JobOpeningService jobOpeningService;
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(value ="/jobopenings", method = RequestMethod.POST)
    public ResponseEntity createJobOpening(HttpServletResponse response, 
                                    @RequestParam Map<String,String> params) 
    {
        String companyId = params.get("companyId");
        
        if (companyId == null) 
        {
            //return the request with error and prompt for company id
        }

        Company company = companyService.getCompany(Integer.valueOf(companyId));
        
        if (company == null)
        {
            //return with error here
        }
        
        String title = params.get("title");
        String description = params.get("description");
        String location = params.get("location");
        String salary = params.get("salary");
        String responsibilties = params.get("responsibilities");
      
        JobOpening jobopening = jobOpeningService.createJobOpening(company, title, description, 
                                    responsibilties, location, salary);
        if (jobopening == null) {
            log.error("job opening is null");
        } else {
            log.error(jobopening.getCompanyname());
        }
        return new ResponseEntity<>(companyService.getJobopeningInCompany(company,jobopening), 
                                            new HttpHeaders(), HttpStatus.OK);

    }

     @RequestMapping(value ="/company/{companyId}/jobopenings", method = RequestMethod.GET)
     public ResponseEntity getJobOpeningsInCompany( HttpServletResponse response, 
                                            @PathVariable String companyId)
    {
        Company company = companyService.getCompany(Integer.valueOf(companyId));
        
        if (company == null)
        {
            //return with error here
        }

        List<JobOpening> jobOpeningList = new ArrayList<>();
        jobOpeningList = jobOpeningService.getJobOpeningsInCompany(companyId);
        return new ResponseEntity<>(companyService.getAllJobOpenings(company, jobOpeningList),
                                    new HttpHeaders(), HttpStatus.OK);
    }

    

    /*
    @RequestMapping(value ="/jobopening", method = RequestMethod.POST)
    public void updateJobOpening(HttpServletResponse response, 
                                    @RequestParam Map<String,String> params) 
    {
        log.error("Creating an opening");
    }


    @RequestMapping(value ="/jobopening", method = RequestMethod.GET)
    public void getJobOpening(HttpServletResponse response, 
                                    @RequestParam Map<String,String> params) 
    {
        log.error("Creating an opening");
    }
    */

}
