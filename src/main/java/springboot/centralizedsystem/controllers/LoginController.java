package springboot.centralizedsystem.controllers;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.resources.Keys;
import springboot.centralizedsystem.resources.Messages;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.utils.HttpUtils;
import springboot.centralizedsystem.utils.ValidateUtils;

@Controller
public class LoginController {

    @GetMapping(value = { RequestsPath.NONE, RequestsPath.SLASH, RequestsPath.LOGIN })
    public String loginGET(Model model, @ModelAttribute(Keys.LOGIN) String error) {
        model.addAttribute("title", "Login");
        model.addAttribute(Keys.UNKNOWN_TYPE_USER, new User("xtreme@admin.io"));
        if (!error.equals("")) {
            model.addAttribute("error", error);
        }
        return Views.LOGIN;
    }

    @PostMapping(RequestsPath.LOGIN)
    public String loginPOST(@Valid User user, Model model, HttpSession session, RedirectAttributes redirect) {
        try {
            String email = user.getEmail();
            String reqJSON = "{\"data\":{\"email\":\"" + email + "\",\"password\":\"" + user.getPassword() + "\"}}";

            HttpEntity<String> entity = new HttpEntity<>(reqJSON, HttpUtils.getHeader());

            ResponseEntity<String> res = null;
            // Login
            try {
                res = new RestTemplate().postForEntity(APIs.LOGIN_URL, entity, String.class);
            } catch (HttpClientErrorException e) {
                switch (e.getStatusCode()) {
                case UNAUTHORIZED:
                    redirect.addFlashAttribute(Keys.LOGIN, Messages.INVALID_ACCOUNT_ERROR);
                    return "redirect:" + RequestsPath.LOGIN;
                case NOT_FOUND:
                    return Views.ERROR_404;
                default:
                    return Views.ERROR_UNKNOWN;
                }
            }

            // Get token from response header
            String token = res.getHeaders().get(APIs.TOKEN_KEY).get(0);
            JSONObject resJSON = new JSONObject(res.getBody());
            JSONObject dataJSON = resJSON.getJSONObject("data");

            boolean isAdmin = false;
            if (ValidateUtils.isEmptyString(dataJSON, "permission")) {
                isAdmin = true;
            }

            session.setAttribute(Keys.IS_ADMIN, isAdmin);
            if (isAdmin) {
                // Return dashboard in Administrator Page
                session.setAttribute(Keys.USER, new User(email, dataJSON.getString("name"), token));
                return "redirect:" + RequestsPath.DASHBOARD;
            }

            if (dataJSON.getInt("status") == Configs.DEACTIVE_STATUS) {
                // User had been blocked
                redirect.addFlashAttribute(Keys.LOGIN, Messages.DEACTIVE_USER);
                return "redirect:" + RequestsPath.LOGIN;
            }

            // Set User Info
            String id = resJSON.getString("_id");
            String name = dataJSON.getString("name");
            String idGroup = dataJSON.getString("idGroup");
            String gender = dataJSON.getString("gender");
            String phoneNumber = "";
            if (!ValidateUtils.isEmptyString(dataJSON, "phoneNumber")) {
                phoneNumber = dataJSON.getString("phoneNumber");
            }
            String address = "";
            if (!ValidateUtils.isEmptyString(dataJSON, "address")) {
                address = dataJSON.getString("address");
            }
            session.setAttribute(Keys.USER,
                    new User(email, name, token, idGroup, gender, phoneNumber, address, id));

            redirect.addAttribute("page", 1);
            return "redirect:" + RequestsPath.REPORTS;
        } catch (HttpServerErrorException e) {
            switch (e.getStatusCode()) {
            case INTERNAL_SERVER_ERROR:
                return Views.ERROR_500;
            default:
                return Views.ERROR_UNKNOWN;
            }
        } catch (Exception e) {
            return Views.ERROR_UNKNOWN;
        }
    }

    @GetMapping(RequestsPath.LOGOUT)
    public String logoutGET(HttpSession session) {
        HttpEntity<String> entity = new HttpEntity<>(HttpUtils.getHeader());
        new RestTemplate().exchange(APIs.LOGOUT_URL, HttpMethod.GET, entity, String.class);

        session.invalidate();

        return "redirect:" + RequestsPath.LOGIN;
    }
}
