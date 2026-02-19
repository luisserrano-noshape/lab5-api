package cncs.academy.ess.controller;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.service.JwtUtil;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.casbin.jcasbin.main.Enforcer;
import io.javalin.http.ForbiddenResponse;

public class AuthorizationMiddleware implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationMiddleware.class);
    private final UserRepository userRepository;
    private final Enforcer enforcer;

    public AuthorizationMiddleware(UserRepository userRepository) {

        this.userRepository = userRepository;
        this.enforcer = new Enforcer("model.conf", "policy.csv");
    }

    @Override
    public void handle(Context ctx) throws Exception {

        if (ctx.header("Access-Control-Request-Headers") != null) {
            return;
        }


        if (ctx.path().equals("/login") && ctx.method().name().equals("POST")) {
            return;
        }

      /*  if (ctx.path().equals("/user") && ctx.method().name().equals("POST")
        || ctx.path().equals("/login") && ctx.method().name().equals("POST")) {
            return;
        }*/


        String authorizationHeader = ctx.header("Authorization");
        String path = ctx.path();
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.info("Authorization header is missing or invalid '{}' for path '{}'", authorizationHeader, path);
            throw new UnauthorizedResponse();
        }

        // Extract token from authorization header
        String token = authorizationHeader.substring(7); // Remove "Bearer "

        // Check if token is valid (perform authentication logic)
        //int userId = validateTokenAndGetUserId(ctx, token);
        User user = validateTokenAndGetUser(ctx, token);
        if (user == null) {
            logger.info("Authorization token is invalid {}", token);
            throw new UnauthorizedResponse();
        }
        String sub = user.getRole();
        String obj = ctx.path();
        String act = ctx.method().name();

        if (!enforcer.enforce(sub, obj, act)) {
            logger.warn("Casbin: Acesso negado a {} em {}", sub, obj);
            throw new ForbiddenResponse("Não tens permissão para realizar esta ação.");
        }
        ctx.attribute("userId", user.getId());
      /*  if (userId == -1) {
            logger.info("Authorization token is invalid {}", token  );
            throw new UnauthorizedResponse();
        }*/

        // Add user ID to context for use in route handlers
        //ctx.attribute("userId", userId);
    }


    private User validateTokenAndGetUser(Context ctx, String token) {
        String username = JwtUtil.validateToken(token);
        if (username == null) return null;

        return userRepository.findByUsername(username);
    }
    /*private int validateTokenAndGetUserId(Context cts, String token) {


        User user = userRepository.findByUsername(JwtUtil.validateToken(token));
        if (user == null) {
            // user not found, token is invalid
            return -1;
        }
        return user.getId();
    }*/
    /*private int validateTokenAndGetUserId(Context cts, String token) {
        User user = userRepository.findByUsername(token);
        if (user == null) {
            // user not found, token is invalid
            return -1;
        }
        return user.getId();
    }*/
}

