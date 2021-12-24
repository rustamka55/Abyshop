package com.web.jwtauth.payload.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor

public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20,message = "username size")
    private String username;

    @NotBlank
    @Size(max = 50,message = "em size")
    @Email
    private String email;

    private Set<String> role;

    @NotBlank
    @Size(min = 6, max = 40,message = "pass size")
    private String password;

}