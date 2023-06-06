package com.web.jwtauth.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSessionRequest {

    private @NotBlank String time;

    private @NotBlank String duration;
    private @NotBlank String date;
    private @NotBlank String room;
}
