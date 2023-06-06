package com.web.jwtauth.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeEmailRequest {

    @NotBlank
    @Size(max = 50,message = "em size")
    @javax.validation.constraints.Email
    private String Email;
    @NotBlank
    private String password;
}
