package com.generator;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import org.json.JSONObject;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@RestController
@RequestMapping(path="/homedata")
public class HomedataController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UsageRepository usageRepository;

    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 30) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    /**
     * Validates Google reCAPTCHA V2 or Invisible reCAPTCHA.
     * @param response reCAPTCHA response from client side. (g-recaptcha-response)
     * @return true if validation successful, false otherwise.
     */
    public synchronized boolean isCaptchaValid(String response) {
        String secretKey = "6LfyMbkUAAAAADYUmfs44l7m7hHeFAzIXyaB_8qv";
        try {
            String url = "https://www.google.com/recaptcha/api/siteverify?"
                    + "secret=" + secretKey
                    + "&response=" + response;
            InputStream res = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(res, Charset.forName("UTF-8")));

            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            String jsonText = sb.toString();
            res.close();

            JSONObject json = new JSONObject(jsonText);
            return json.getBoolean("success");
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return false;
    }

    private static Random rand = new Random((new Date()).getTime());
    protected static String cypherPassword(String text, String type) throws Exception{

        if (type.equals("encrypt"))
        {
            try {
                BASE64Encoder encoder = new BASE64Encoder();
                byte[] salt = new byte[8];
                rand.nextBytes(salt);
                return encoder.encode(salt) + encoder.encode(text.getBytes());

            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception(e);
            }
        }else{
            try {
                if (text.length() > 12) {
                    String cipher = text.substring(12);
                    BASE64Decoder decoder = new BASE64Decoder();
                    try {
                        return new String(decoder.decodeBuffer(cipher));
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new Exception(e);
                    }

                }
                return null;

            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception(e);
            }
        }
    }

    private Boolean loginCheck(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               HttpSession session)
    {
        User user = userRepository.findByEmail(email);

        if (user != null) {
            String passwordFromDb = user.getPassword() + "";

            String decryptPassword = null;
            try {
                decryptPassword = this.cypherPassword(passwordFromDb, "decrypt");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (decryptPassword.equals(password)) {
                session.setAttribute("loggedInUser", email);
                return true;
            } else {
                return false;
            }
        }else {
            return false;
        }
    }

    @RequestMapping(path="/getUserData")
    public String userData(HttpSession session)
    {
        String email = (String) session.getAttribute("loggedInUser");
        User user = userRepository.findByEmail(email);

        Integer Id = user.getId();
        String name = user.getName();
        String nickname = name.substring(0, name.indexOf(' '));
        String token = user.getToken();

        Integer usedTimes = usageRepository.countUsage(Id);

        JsonObject data = new JsonObject();
        data.addProperty("User_id", Id);
        data.addProperty("Name", name);
        data.addProperty("Nickname", nickname);
        data.addProperty("Token", token);
        data.addProperty("Usage_times", usedTimes);

        return data.toString();
    }

    @RequestMapping(path="/getUsageHistory")
    public List usageHistory(HttpSession session)
    {
        List history = usageRepository.findAllByUserId(
                userRepository.getIdByEmail(
                        (String) session.getAttribute("loggedInUser")
                )
        );

        return history;
    }

    @PostMapping(path="/registerUser") // Map ONLY POST Requests
    public @ResponseBody
    String addNewUser (@RequestParam String name,
                       @RequestParam String email,
                       @RequestParam String password,
                       @RequestParam String captcha,
                       HttpSession session) {

        if (!this.isCaptchaValid(captcha)) {
            JsonObject response = new JsonObject();
            response.addProperty("status", "failed");
            response.addProperty("status_message", "Invalid captcha!");

            return response.toString();
        }

        if (session.getAttribute("loggedInUser") != null) {
            JsonObject response = new JsonObject();
            response.addProperty("status", "failed");
            response.addProperty("status_message", "Oh noes, something`s wrong!");

            return response.toString();
        }

        String encryptPassword = null;
        try {
            encryptPassword = this.cypherPassword(password, "encrypt");
        } catch (Exception e) {
            e.printStackTrace();
        }

        User n = new User();
        n.setName(name+" ");
        n.setEmail(email);
        n.setPassword(encryptPassword);
        n.setToken(this.getSaltString());
        userRepository.save(n);

        JsonObject response = new JsonObject();
        response.addProperty("status", "success");
        response.addProperty("status_message", "Done, we will redirect you to login page shortly!");

        return response.toString();
    }

    @RequestMapping(path = "/loginUser", method = RequestMethod.POST)
    public String login(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("captcha") String captcha,
            HttpSession session,
            HttpServletResponse httpResponse) {

        if (!this.isCaptchaValid(captcha))
        {
            JsonObject response = new JsonObject();
            response.addProperty("status", "failed");
            response.addProperty("status_message", "Invalid captcha!");

            return response.toString();
        }

        if (session.getAttribute("loggedInUser") != null)
        {
            JsonObject response = new JsonObject();
            response.addProperty("status", "failed");
            response.addProperty("status_message", "Oh noes, something`s wrong!");

            return response.toString();
        }

        Boolean loginStatus = this.loginCheck(email, password, session);

        if(loginStatus == true) {
            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.addProperty("status_message", "Login successful, we will redirect you to welcome page shortly!");

            return response.toString();
        } else {
            JsonObject response = new JsonObject();
            response.addProperty("status", "failed");
            response.addProperty("status_message", "Email or password is wrong!");

            return response.toString();
        }
    }


}