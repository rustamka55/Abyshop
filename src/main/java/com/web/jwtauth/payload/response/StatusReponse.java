package com.web.jwtauth.payload.response;

public class StatusReponse {
    private Boolean status;

    public StatusReponse(Boolean status) {
        this.status = status;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
