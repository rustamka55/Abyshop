package com.web.jwtauth.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddGenreRequest {
    @NotBlank
    private Optional<String> title  = Optional.empty();

    @NotBlank
    private Optional<String> description  = Optional.empty();

}