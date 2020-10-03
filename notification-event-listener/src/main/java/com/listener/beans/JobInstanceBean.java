package com.listener.beans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class JobInstanceBean implements Serializable {

    private static final long serialVersionUID = -8821457884847246912L;

    @JsonProperty(value = "ID", required = true)
    private String id;

    @JsonProperty("PREDICATE_GROUP_NAME")
    private String predicateGroupName;

    @JsonProperty("PREDICATE_JOB_NAME")
    private String predicateJobName;

    @JsonProperty("SESSIONID")
    private String sessionId;

    @JsonProperty("STATE")
    private String state;

    @JsonProperty("TYPE")
    private String type;

    @JsonProperty("JOB_ID")
    private String jobId;

}
