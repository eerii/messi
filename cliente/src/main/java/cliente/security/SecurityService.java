package cliente.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {

    private final AuthenticationContext ctx;

    public SecurityService(AuthenticationContext ctx) {
        this.ctx = ctx;
    }

    public void logout() {
        ctx.logout();
    }
}