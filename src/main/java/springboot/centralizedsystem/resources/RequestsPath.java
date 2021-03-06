package springboot.centralizedsystem.resources;

public class RequestsPath {

    public static final String NONE = "";
    public static final String SLASH = "/";
    public static final String ERROR_404 = "/error-404";
    public static final String LOGIN = "/login";
    public static final String REGISTER = "/register";
    public static final String LOGOUT = "/logout";
    public static final String DASHBOARD = "/dashboard";
    public static final String FORMS = "/forms/{page}";
    public static final String SUBMISSIONS = "/submissions/{path}/{page}";
    public static final String EXPORT_SUBMISSIONS = "/export/{type}/{path}";
    public static final String FORM = "/form";
    public static final String CREATE_FORM = "/build/form";
    public static final String EDIT_FORM = "/build/form/{path}";
    public static final String BUILDER = "/builder";
    public static final String DELETE_FORM = "/delete/form/{path}";
    public static final String REPORTS = "/reports/{page}";
    public static final String SEND_REPORT_AUTHENTICATED = "/send/auth/report/{path}";
    public static final String SEND_REPORT_ANONYMOUS = "/send/anon/report/{path}";
    public static final String EDIT_REPORT = "/edit/report/{path}";
    public static final String STATISTICS = "/statistics";
    public static final String STATISTICAL_ANALYSIS = "/analysis/{path}";
    public static final String READ_SURVEY = "/survey/read";
    public static final String USERS = "/users/{idGroup}/{page}";
    public static final String CREATE_USER = "/build/user";
    public static final String EDIT_USER = "/build/user/{id}";
    public static final String READ_USERS = "/users/read";
    public static final String PROFILE = "/profile";
    public static final String EDIT_PROFILE = "/edit/profile";
    public static final String GROUPS = "/groups/{idParent}/{page}";
    public static final String CREATE_GROUP = "/build/group";
    public static final String EDIT_GROUP = "/build/group/{id}";
    public static final String AJAX_GROUPS = "/ajax/groups";
    public static final String READ_GROUPS = "/groups/read";
}
