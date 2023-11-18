package cliente;

import java.rmi.RemoteException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends AppLayout {
    ClienteImpl cliente;

    public MainView() {
        // Esperamos a que la aplicación incialice el cliente y se haya conectado
        while (cliente == null) {
            cliente = App.get();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        // Barra superior
        DrawerToggle toggle = new DrawerToggle();
        H1 title = new H1("Chat");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");
        addToNavbar(toggle, title);

        // Barra lateral
        List<String> nombres = new ArrayList<>(List.of("usuario 1", "usuario 2", "usuario 3"));
        for (ICliente c : cliente.clientes)
            try {
                nombres.add(c.str());
            } catch (RemoteException e) {
            }

        for (String n : nombres) {
            SideNavItem chat = new SideNavItem(n, MainView.class, VaadinIcon.USER.create());

            int i = ThreadLocalRandom.current().nextInt(0, 10);
            Span no_leido = new Span(String.valueOf(i));
            no_leido.getElement().getThemeList().add("badge contrast pill");

            chat.setSuffixComponent(no_leido);
            addToDrawer(chat);
        }

        setPrimarySection(Section.DRAWER);

        // Contenido
        MessageList mensajes = new MessageList();

        MessageInput input = new MessageInput();
        input.addSubmitListener(submitEvent -> {
            MessageListItem msg = new MessageListItem(
                    submitEvent.getValue(), Instant.now(), "Harry Harrison");
            msg.setUserColorIndex(3);
            List<MessageListItem> ml = new ArrayList<>(mensajes.getItems());
            ml.add(msg);
            mensajes.setItems(ml);
        });

        MessageListItem msg1 = new MessageListItem(
                "Nature does not hurry, yet everything gets accomplished.",
                LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC),
                "Matt Mambo");
        msg1.setUserColorIndex(1);
        MessageListItem msg2 = new MessageListItem(
                "Using your talent, hobby or profession in a way that makes you contribute with something good to this world is truly the way to go.",
                LocalDateTime.now().minusMinutes(55).toInstant(ZoneOffset.UTC),
                "Linsey Listy");
        msg2.setUserColorIndex(2);
        mensajes.setItems(msg1, msg2);

        VerticalLayout layout = new VerticalLayout(mensajes, input);
        layout.expand(mensajes);

        setContent(layout);
    }
}
