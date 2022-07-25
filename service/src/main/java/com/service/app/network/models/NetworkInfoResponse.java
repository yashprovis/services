package com.service.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkInfoResponse {

    private String query;


    public NetworkInfoResponse(
            @JsonProperty("ip") String query
          ) {
        this.query = query;

    }

    public String getQuery() {
        return query;
    }


}
