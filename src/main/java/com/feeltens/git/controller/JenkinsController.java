package com.feeltens.git.controller;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.common.IntegerResponse;
import com.cdancy.jenkins.rest.domain.common.RequestStatus;
import com.cdancy.jenkins.rest.domain.crumb.Crumb;
import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.cdancy.jenkins.rest.domain.job.Job;
import com.cdancy.jenkins.rest.domain.job.JobInfo;
import com.cdancy.jenkins.rest.domain.job.JobList;
import com.cdancy.jenkins.rest.domain.system.SystemInfo;
import com.cdancy.jenkins.rest.features.JobsApi;
import com.feeltens.git.vo.base.CloudResponse;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JenkinsController
 * <p>
 * 操作 jenkins
 *
 * @author feeltens
 * @date 2025-09-30
 */
@RestController
@RequestMapping("/api/v1/jenkins")
@Slf4j
public class JenkinsController {

    /**
     * testJenkins
     */
    @PostMapping("/testJenkins")
    public CloudResponse<String> testJenkins() {
        try {
            long start = System.currentTimeMillis();
            // 构建 jenkins client
            JenkinsClient client = JenkinsClient.builder()
                    .endPoint("http://192.168.111.221:9090") // Optional. Defaults to http://127.0.0.1:8080
                    // .credentials("zhangsan:zhangsan") // user:password
                    .apiToken("zhangsan:1112a8b2c5ad3e56b633cd6459f919cc30") // user:apiToken
                    // base64 encoded value
                    .build();

            Crumb crumb = client.api().crumbIssuerApi().crumb();

            SystemInfo systemInfo = client.api().systemApi().systemInfo();
            log.info("jenkins version:{}", systemInfo.jenkinsVersion());

            // 查询 job
            JobsApi jobsApi = client.api().jobsApi();
            JobList jobList = jobsApi.jobList("");
            String jobName = "gitlab_sample_job";
            jobList.jobs().forEach(job -> log.info("jobName:{}  jobUrl:{}  jobColor:{}", job.name(), job.url(), job.color()));
            Optional<Job> jobOpt = jobList.jobs().stream().filter(job -> job.name().equals(jobName)).findFirst();
            if (jobOpt.isPresent()) {
                Job job = jobOpt.get();
                String url = job.url();
                log.info("job url:{}", url);
            }

            // job信息
            JobInfo jobInfo = jobsApi.jobInfo(null, jobName);
            // 上一次构建
            BuildInfo lastBuild = jobInfo.lastBuild();
            // stop 上一次构建
            RequestStatus stopRes = jobsApi.stop(null, jobName, lastBuild.number());
            log.info("lastBuildNumber:{} stopResFlag:{}", lastBuild.number(), stopRes.value());

            Map<String, List<String>> properties = Maps.newHashMap();
            // properties.put("branch", Collections.singletonList("master")); // 分支名称
            // properties.put("Jenkins-Crumb", Collections.singletonList(crumb.value()));
            // properties.put("Content-Type", Collections.singletonList("application/json"));

            // 重新构建
            String encodedJobName = URLEncoder.encode(jobName, String.valueOf(StandardCharsets.UTF_8));
            IntegerResponse buildRes = jobsApi.build(null, encodedJobName);
            // IntegerResponse buildRes = jobsApi.buildWithParameters(null, encodedJobName, properties);
            if (CollUtil.isNotEmpty(buildRes.errors())) {
                buildRes.errors().forEach(error -> log.error("error:{}  exceptionName:{}", error.message(), error.exceptionName()));
            }
            log.info("build buildResVal:{}  errorList:{}", buildRes.value(), JSON.toJSONString(buildRes.errors()));

            // DeleteGitBranchRespVO res = gitFlowService.deleteGitBranch(req);
            log.info("testJenkins hasResult,    result:{}    costTime:{}ms",
                    JSON.toJSONString(buildRes), System.currentTimeMillis() - start);
            return CloudResponse.success(JSON.toJSONString(buildRes));
        } catch (Exception e) {
            log.error("testJenkins hasError,   e:", e);
            return CloudResponse.fail(e.getMessage());
        }
    }

}