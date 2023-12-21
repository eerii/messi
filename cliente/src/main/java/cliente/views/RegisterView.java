package cliente.views;

import java.rmi.RemoteException;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import cliente.ClienteImpl;

@Route("register")
@PageTitle("Register | Mess")
@AnonymousAllowed
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

	public RegisterView() {
		addClassName("register-view");
		setSizeFull();
		setAlignItems(Alignment.CENTER);
		setJustifyContentMode(JustifyContentMode.CENTER);

		H1 title = new H1("Register");
		add(title);

		HorizontalLayout layout = new HorizontalLayout();

		FormLayout formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
		formLayout.setWidth("450px");

		TextField usernameField = new TextField("Username");
		formLayout.add(usernameField);

		PasswordField passwordField = new PasswordField("Password");
		formLayout.add(passwordField);

		Button registerButton = new Button("Registrarse");
		registerButton.addClickListener(event -> {
			String username = usernameField.getValue();
			String password = passwordField.getValue();

			try {
            	ClienteImpl.get().registrarse(username, password);
			} catch (RemoteException e) {
				Notification.show("Error al registrarse");
				System.out.println(e);
				return;
			}

			Notification.show("Registration successful!");
			getUI().ifPresent(ui -> ui.navigate("login"));
		});
		registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		formLayout.add(registerButton);

		Button loginButton = new Button("Login");
		loginButton.addClickListener(event -> {
			getUI().ifPresent(ui -> ui.navigate("login"));
		});
		loginButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		formLayout.add(loginButton);

		layout.add(formLayout);
		layout.setJustifyContentMode(JustifyContentMode.CENTER);
		layout.setAlignItems(Alignment.CENTER);
		layout.setWidthFull();

		add(layout);
	}
	
	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// Perform any necessary setup or validation before entering the view
	}
}