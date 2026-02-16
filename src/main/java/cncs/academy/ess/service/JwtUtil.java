package cncs.academy.ess.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import  com.auth0.jwt.interfaces.DecodedJWT;
import  com.auth0.jwt.interfaces.JWTVerifier;
import java.util.Date;


public class JwtUtil {


    private static final String SECRET_KEY = "sercre_secret_secret_grupo10";
    private static final long EXPIRATION_TIME = 900_000;

    public static String generateToken(String username) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

        return JWT.create()
                .withIssuer("tasklist-backend")
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public static String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("tasklist-backend")
                    .build();

            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("username").asString();
        } catch (Exception e) {
            return null; // Token falhou na validação
        }
    }
}