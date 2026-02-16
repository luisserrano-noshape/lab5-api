package cncs.academy.ess.controller;

import cncs.academy.ess.controller.messages.ErrorMessage;
import cncs.academy.ess.controller.messages.UserAddRequest;
import cncs.academy.ess.controller.messages.UserLoginRequest;
import cncs.academy.ess.controller.messages.UserResponse;
import cncs.academy.ess.model.User;
import cncs.academy.ess.service.TodoUserService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Novos imports necessários para a vulnerabilidade Zip Slip
import java.io.*;
import java.util.zip.*;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;

public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final TodoUserService userService;

    public UserController(TodoUserService userService) {
        this.userService = userService;
    }

    /**
     * Ponto 2.f: Método vulnerável a Zip Slip (CWE-22)
     * Este método recebe um ZIP e extrai o conteúdo sem validar os nomes dos ficheiros.
     */
    public void addProfilePicture(Context ctx) {
        String userId = ctx.pathParam("userId");
        String destinationDir = "/app/profiles/" + userId;
        
        log.info("Uploading profile pictures for user: {}", userId);

        try (InputStream zipInput = ctx.uploadedFile("profileZip").content();
             ZipInputStream zis = new ZipInputStream(zipInput)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // VULNERABILIDADE: Uso direto de entry.getName() sem validação de ".."
                // Um atacante pode enviar um ficheiro chamado "../../../etc/passwd"
                File profilePic = new File(destinationDir, entry.getName());

                // Cria as diretorias sem validar se saímos de /app/profiles/
                profilePic.getParentFile().mkdirs();

                // Extração insegura
                Files.copy(zis, profilePic.toPath(), StandardCopyOption.REPLACE_EXISTING);
                zis.closeEntry();
            }
            ctx.status(200).json("Profile pictures uploaded successfully");
        } catch (Exception e) {
            log.error("Error in Zip Slip extraction: ", e);
            ctx.status(500).result("Error uploading profile pictures");
        }
    }

    public void createUser(Context ctx) throws NoSuchAlgorithmException {
        UserAddRequest userRequest = ctx.bodyAsClass(UserAddRequest.class);
        log.info("Create user: {}", userRequest.username);
        User user = userService.addUser(userRequest.username, userRequest.password, userRequest.role);
        UserResponse response = new UserResponse(user.getId(), user.getUsername());
        ctx.status(201).json(response);
    }

    public void getUser(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));
        User user = userService.getUser(userId);
        if (user != null) {
            UserResponse response = new UserResponse(user.getId(), user.getUsername());
            ctx.status(200).json(response);
        } else {
            ctx.status(404).json(new ErrorMessage("User not found"));
        }
    }

    public void deleteUser(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));
        userService.deleteUser(userId);
        ctx.status(204);
    }

    public void loginUser(Context ctx) throws NoSuchAlgorithmException {
        UserLoginRequest userRequest = ctx.bodyAsClass(UserLoginRequest.class);
        log.info("Login user: {}", userRequest.username);
        String token = userService.login(userRequest.username, userRequest.password);
        if (token != null) {
            ctx.status(200).json(token);
        } else {
            ctx.status(401).json(new ErrorMessage("Invalid username or password"));
        }
    }
}
