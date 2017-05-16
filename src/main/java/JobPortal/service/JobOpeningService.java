package JobPortal.service;

import JobPortal.Dao.JobOpeningDao;
import JobPortal.model.JobOpening;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.TermMatchingContext;

import JobPortal.model.Company;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import org.apache.commons.collections.CollectionUtils;

import javax.transaction.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import com.google.gson.Gson;


/**
 * Created by ipshamohanty on 5/1/17.
 */

@Service
@Transactional
public class JobOpeningService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JobOpeningDao jobOpeningDao;

    public JobOpeningService() {

    }

    public JobOpeningService(JobOpeningDao jobopeningDao) {
        this.jobOpeningDao = jobOpeningDao;
    }

    public JobOpening getJobOpening(int jobId) {

        JobOpening jobOpening = null;
        return jobOpeningDao.findByJobId(jobId);
    }
    

    public JobOpening createJobOpening(Company company, String title, String description,
                String responsibilities, String location, String salary)
    {

        JobOpening jobOpening; 
        try {       
            jobOpening = new JobOpening(Integer.valueOf(company.getCompanyId()), company.getCompanyname(),  
                                     title, description, responsibilities, location, 
                                     Integer.valueOf(salary));
            JobOpening newJobOpening = jobOpeningDao.save(jobOpening);
            return newJobOpening;
        } catch (Exception e)
        {
            return null;
        }
    } 
   
    //@Transactional(readOnly = true)
    public List<JobOpening> getJobOpeningsInCompany(String companyId)
    {
       String query = "select * from job_openings where job_openings.companyId = " + companyId;
       List<JobOpening> jobOpeningList = new ArrayList<>();
       jobOpeningList = jobOpeningDao.findJobOpeningsInCompany(companyId);
       return jobOpeningList;
    }

    public List<JobOpening> getJobOpeningsInCompany(String companyId, List<String> statuslist)
    {
       List<JobOpening> jobOpeningList = new ArrayList<>();
       jobOpeningList = jobOpeningDao.findJobOpeningsInCompanyByStatus(companyId, statuslist);
       return jobOpeningList;
    } 


    public JobOpening getJobOpeningByJobId(String jobId)
    {
       JobOpening jobOpening = jobOpeningDao.findJobOpeningByJobId(Integer.valueOf(jobId));
       return jobOpening;  
    }

    public String getJobOpeningsByFilters(String companynames, String locations,
                                                    String salaryStart, String salaryEnd)
    {
            List<JobOpening> jobOpenings = new ArrayList<>();
            List<Integer> jobIds = new ArrayList<>();
            List<Integer> companiesList = new ArrayList<>();
            List<Integer> locationsList = new ArrayList<>();
            List<Integer> salaryList = new ArrayList<>();

            //Check if company list is present
            if (!companynames.equals("")) {
                List<String> companynamesList = Arrays.asList(companynames.split("\\s*,\\s*"));
                companiesList = jobOpeningDao.findJobOpeningsInCompanyByName(companynamesList); 
            } 
            if (!locations.equals("")) {
                List<String> locationNamesList = Arrays.asList(locations.split("\\s*,\\s*"));
                locationsList = jobOpeningDao.
                                findJobOpeningsInCompanyByLocation(locationNamesList); 
            }
            if (!(salaryStart.equals("") && salaryEnd.equals("")))
            {
               salaryList = jobOpeningDao.findJobOpeningsInCompanyBySalary
                            (Integer.valueOf(salaryStart), Integer.valueOf(salaryEnd));
            }

            Collection<Integer> collection1 = companiesList;
            Collection<Integer> collection2 = locationsList;
            Collection<Integer> collection3 = salaryList;
            Collection<Integer> intersection = new ArrayList<Integer>();
            if (!companiesList.isEmpty() && !locationsList.isEmpty()) {
                intersection = (Collection<Integer>)CollectionUtils.
                                        intersection(collection1, collection2);
            } else if (!companiesList.isEmpty() && locations.equals("")) {
                intersection = companiesList;
            } else if (!locationsList.isEmpty() && companynames.equals("")) {
                intersection = locationsList;
            }
       
            if (!salaryStart.equals("")) {
                if (companynames.equals("") && locations.equals("")) {
                    intersection = salaryList;
                } else {
                    intersection = (Collection<Integer>)CollectionUtils.
                                        intersection(intersection, collection3);
                }
                    
            }
            jobIds.addAll(intersection);
            if (!jobIds.isEmpty()) {
                jobOpenings = jobOpeningDao.findJobOpeningsInCompanyByFilter(jobIds);
            }
            
            LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
            map.put("jobopenings", jobOpenings);
            Gson gson = new Gson();
            String jobOpeningsJson = gson.toJson(map, LinkedHashMap.class);
            return jobOpeningsJson; 
    }
    
    
     public List<JobOpening> getAllOpenJobs() {
        List<JobOpening> jobOpeningList = new ArrayList<>();
        jobOpeningList = jobOpeningDao.getAllJobs();

        return jobOpeningList;
    }

    public ModelMap getAllFilters() {
        List<String> companyList = new ArrayList<>();
        companyList = companyDao.getAllCompanies();

        List<String> locationList = new ArrayList<>();
        locationList = jobOpeningDao.getAllLocations();

        ModelMap m = new ModelMap();
        m.addAttribute("companies", companyList);
        m.addAttribute("locations", locationList);

        return m;
    }


    public String searchJobOpenings(String text) {
    
        boolean emptyListReturned = false;
        // get the full text entity manager
        FullTextEntityManager fullTextEntityManager =
          org.hibernate.search.jpa.Search.
          getFullTextEntityManager(entityManager);
        
        // create the query using Hibernate Search query DSL
        QueryBuilder queryBuilder = 
          fullTextEntityManager.getSearchFactory()
          .buildQueryBuilder().forEntity(JobOpening.class).get();
        
        List<String> queryList = Arrays.asList(text.split("\\s*,\\s*"));
        Map<String, List<JobOpening>> jobMap = new HashMap<String, List<JobOpening>>();
        for (String queryText : queryList) 
        {
             //a very basic query by keywords
            org.apache.lucene.search.Query query =
              queryBuilder
                .keyword()
                .onFields("title", "location", "responsibilities", "description" , "companyname")
                .matching(queryText)
                .createQuery();

             // wrap Lucene query in an Hibernate Query object
            org.hibernate.search.jpa.FullTextQuery jpaQuery =
              fullTextEntityManager.createFullTextQuery(query, JobOpening.class);
      
            // execute search and return results (sorted by relevance as default)
            @SuppressWarnings("unchecked")
            List<JobOpening> results = jpaQuery.getResultList();
        
            if (results.size() == 0)
            {
                emptyListReturned = true;
                break;
            }
            jobMap.put(queryText,results);
        }
      
        Collection<JobOpening> intersection = new ArrayList<JobOpening>();
 
        for (String key : jobMap.keySet())
        {
            if (emptyListReturned)
                break;
            Collection<JobOpening> jobCollection = jobMap.get(key);
            if (!intersection.isEmpty()) {
                intersection = (Collection<JobOpening>)CollectionUtils.
                                        intersection(intersection, jobCollection);
                if (intersection.isEmpty()) {
                    emptyListReturned = true;
                    break;
                }
            } else {
                intersection = jobCollection;
            }

        }
        List<JobOpening> results  = new ArrayList<>();
        results.addAll(intersection);
        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        if (!emptyListReturned) 
            map.put("jobopenings", results);
        Gson gson = new Gson();
        String jobOpeningsJson = gson.toJson(map, LinkedHashMap.class);
        return jobOpeningsJson;
   } 

    
     public ResponseEntity updateJob(int jobid, int companyId, String companyname, String title, String description,
                                    String responsibilities, String location, int salary, String status) {

        JobOpening jobOpening = jobOpeningDao.findJobOpeningByJobId(jobid);

        if (jobOpening == null) {
            //todo : jobopening not found

            ModelMap m = new ModelMap();
            m.addAttribute("msg", "job opening not exists");

            return new ResponseEntity(jobOpening, HttpStatus.BAD_REQUEST);
        }
        try {

            jobOpening = new JobOpening(companyId, companyname, title, description, responsibilities,
                    location, salary, status);


            jobOpening.setJobId(jobid);
            jobOpening = jobOpeningDao.save(jobOpening);


            return new ResponseEntity(jobOpening, HttpStatus.OK);

        } catch(Exception ex) {
            //TODO : Handle exception
            return new ResponseEntity(jobOpening, HttpStatus.BAD_REQUEST);
        }


    }
 
}
