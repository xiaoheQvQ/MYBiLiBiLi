package com.hsx.manyue.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JwtToken
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    public String accessToken;
    public String refreshToken;
}
