package cliente.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {

    private final AuthenticationContext ctx;

    public SecurityService(AuthenticationContext ctx) {
        this.ctx = ctx;
    }

    public UserDetails getAuthenticatedUser() {
        return ctx.getAuthenticatedUser(UserDetails.class).get();
    }

    public void logout() {
        ctx.logout();
    }
}