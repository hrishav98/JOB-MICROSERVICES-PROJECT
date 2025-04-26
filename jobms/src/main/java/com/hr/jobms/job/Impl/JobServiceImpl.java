package com.hr.jobms.job.Impl;

import com.hr.jobms.job.Job;
import com.hr.jobms.job.JobRepository;
import com.hr.jobms.job.JobService;
import com.hr.jobms.job.clients.CompanyClient;
import com.hr.jobms.job.clients.ReviewClient;
import com.hr.jobms.job.dto.JobDTO;
import com.hr.jobms.job.external.Company;
import com.hr.jobms.job.external.Review;
import com.hr.jobms.job.mapper.JobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {
    // private List<Job> jobs = new ArrayList<>();
    JobRepository jobRepository;

    @Autowired
    RestTemplate restTemplate;

    private CompanyClient companyClient;

    private ReviewClient reviewClient;

    public JobServiceImpl(JobRepository jobRepository,CompanyClient companyClient,
                          ReviewClient reviewClient) {
        this.jobRepository = jobRepository;
        this.reviewClient=reviewClient;
        this.companyClient=companyClient;
    }

    @Override
    public List<JobDTO> findAll() {
        List<Job> jobs=jobRepository.findAll();
        //List<JobWithCompanyDTO> jobWithCompanyDTOS =new ArrayList<>();

        //RestTemplate restTemplate=new RestTemplate();
//
//        for(Job job:jobs){
//            JobWithCompanyDTO jobWithCompanyDTO=new JobWithCompanyDTO();
//            jobWithCompanyDTO.setJob(job);
//            Company company = restTemplate.getForObject("http://localhost:8081/companies/"+job.getCompanyId(), Company.class);
//            jobWithCompanyDTO.setCompany(company);
//
//            jobWithCompanyDTOS.add(jobWithCompanyDTO);
//        }
        return jobs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public void createJob(Job job) {
        jobRepository.save(job);
    }

    @Override
    public JobDTO getJobById(Long id) {
       Job job=  jobRepository.findById(id).orElse(null);
       return convertToDTO(job);
    }

    @Override
    public boolean deleteJobById(Long id) {
        try {
            jobRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateJob(Long id, Job updatedJob) {
        Optional<Job> jobOptional = jobRepository.findById(id);
        if (jobOptional.isPresent()) {
            Job job = jobOptional.get();
            job.setTitle(updatedJob.getTitle());
            job.setDescription(updatedJob.getDescription());
            job.setMinSalary(updatedJob.getMinSalary());
            job.setMaxSalary(updatedJob.getMaxSalary());
            job.setLocation(updatedJob.getLocation());
            jobRepository.save(job);
            return true;
        }
        return false;
    }

    private JobDTO convertToDTO(Job job){

        //RestTemplate restTemplate=new RestTemplate();
//        JobWithCompanyDTO jobWithCompanyDTO=new JobWithCompanyDTO();
//        jobWithCompanyDTO.setJob(job);

 /*       Company company = restTemplate.getForObject(
                "http://COMPANYMS/companies/"+job.getCompanyId(),
                 Company.class);*/

        //used openFeign insetead of RESTTEMPLATE for clenerCode
        Company company=companyClient.getCompany(job.getCompanyId());

/*        ResponseEntity<List<Review>> reviewResponse = restTemplate.exchange(
                "http://REIVIEWMS:8083/reviews?companyId=" + job.getCompanyId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Review>>() {
                });*/

        //List<Review> reviews=reviewResponse.getBody();

        List<Review> reviews=reviewClient.getReviews(job.getCompanyId());

        JobDTO jobWithCompanyDTO= JobMapper.
                mapToJobWithCompanyDto(job,company,reviews);
        //jobWithCompanyDTO.setCompany(company);

        return jobWithCompanyDTO;
    }
}