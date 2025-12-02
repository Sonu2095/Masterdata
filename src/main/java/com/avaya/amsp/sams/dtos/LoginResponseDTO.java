package com.avaya.amsp.sams.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
    private Long expiryInMilliseconds;
    private String refreshToken;

}
