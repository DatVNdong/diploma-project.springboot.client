package springboot.centralizedsystem.admin.controllers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.admin.domains.Form;
import springboot.centralizedsystem.admin.domains.FormControl;
import springboot.centralizedsystem.admin.domains.User;
import springboot.centralizedsystem.admin.resources.Keys;
import springboot.centralizedsystem.admin.resources.RequestsPath;
import springboot.centralizedsystem.admin.resources.Views;
import springboot.centralizedsystem.admin.services.FormControlService;
import springboot.centralizedsystem.admin.services.FormService;
import springboot.centralizedsystem.admin.utils.CalculateUtils;
import springboot.centralizedsystem.admin.utils.SessionUtils;

@Controller
public class ReportController extends BaseController {

    @Autowired
    private FormService formService;

    @Autowired
    private FormControlService formControlService;

    private void addFormToList(String token, List<Form> listForm, List<FormControl> listFormControl)
            throws ParseException {
        for (FormControl formControl : listFormControl) {
            String path = formControl.getPathForm();
            String start = formControl.getStart();
            String expired = formControl.getExpired();

            int durationPercent = CalculateUtils.getDurationPercent(start, expired);
            String typeProgressBar = CalculateUtils.getTypeProgressBar(durationPercent);

            ResponseEntity<String> formRes = formService.findOneForm(token, path);
            JSONObject formResJSON = new JSONObject(formRes.getBody());
            String title = formResJSON.getString("title");
            List<String> tags = new ArrayList<>();
            for (Object object : formResJSON.getJSONArray("tags")) {
                tags.add(object.toString());
            }

            ResponseEntity<String> submissionsRes = formService.findAllSubmissions(token, path);
            JSONArray submissionResJSON = new JSONArray(submissionsRes.getBody());
            boolean isSubmitted = !submissionResJSON.isEmpty();

            boolean isPending = CalculateUtils.isFormPending(start);

            listForm.add(new Form(title, path, start, expired, tags, durationPercent, typeProgressBar, isSubmitted,
                    isPending));
        }
    }

    @GetMapping(RequestsPath.REPORTS)
    public String reportsGET(Model model, HttpSession session, RedirectAttributes redirect) throws ParseException {
        try {
            User user = SessionUtils.getUser(session);
            if (SessionUtils.isAdmin(session)) {
                return roleForbidden(redirect);
            }
            if (user == null) {
                return unauthorized(redirect);
            }

            String token = user.getToken();

            List<Form> listForm = new ArrayList<>();

            List<FormControl> listFormsGroup = formControlService.findByAssign(user.getIdGroup());
            addFormToList(token, listForm, listFormsGroup);

            List<FormControl> listFormsAuth = formControlService.findByAssign(Keys.AUTHENTICATED);
            addFormToList(token, listForm, listFormsAuth);

            List<FormControl> listFormsAnon = formControlService.findByAssign(Keys.ANONYMOUS);
            addFormToList(token, listForm, listFormsAnon);

            model.addAttribute("list", listForm);
            model.addAttribute("title", "Reports");

            return Views.REPORTS;
        } catch (HttpClientErrorException e) {
            switch (e.getStatusCode()) {
            case NOT_FOUND:
                return Views.ERROR_404;
            default:
                return Views.ERROR_UNKNOWN;
            }
        } catch (HttpServerErrorException e) {
            switch (e.getStatusCode()) {
            case INTERNAL_SERVER_ERROR:
                return Views.ERROR_500;
            default:
                return Views.ERROR_UNKNOWN;
            }
        }
    }
}