package cliente;

import java.rmi.RemoteException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Aside;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.Orientation;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends HorizontalLayout {
    ClienteImpl cliente;
    List<Chat> chats;
    Chat actual;
    TextField search;
    Tabs tabs;

    public MainView() {
        // Esperamos a que la aplicación incialice el cliente y se haya conectado
        while (cliente == null) {
            cliente = App.get();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        // Estilo
        addClassNames("chat-view", Width.FULL, Display.FLEX, Flex.AUTO);

        // Chats
        chats = new ArrayList<>(List.of(new Chat("Anna", 0), new Chat("Matt", 3), new Chat("Claire", 1)));
        for (ICliente c : cliente.clientes) {
            try {
                chats.add(new Chat(c.str(), 0));
            } catch (RemoteException e) {
            }
        }

        // Barra de chats
        H3 titulo_lateral = new H3("Amistades");

        search = new TextField();
        search.setLabel("Buscar...");
        search.setClearButtonVisible(true);
        search.setPrefixComponent(VaadinIcon.SEARCH.create());

        tabs = new Tabs();
        for (Chat chat : chats) {
            tabs.add(new ChatTab(chat));
        }
        tabs.setOrientation(Orientation.VERTICAL);
        tabs.addClassNames(Flex.GROW, Flex.SHRINK, Overflow.HIDDEN);

        Aside lateral = new Aside();
        lateral.addClassNames(Display.FLEX, FlexDirection.COLUMN, Flex.GROW_NONE, Flex.SHRINK_NONE,
                Background.CONTRAST_5, Padding.MEDIUM);
        lateral.add(titulo_lateral, search, tabs);
        lateral.setWidth("18rem");

        // Contenido
        MessageList mensajes = new MessageList();

        MessageInput input = new MessageInput();
        input.addSubmitListener(submitEvent -> {
            MessageListItem msg = new MessageListItem(
                    submitEvent.getValue(), Instant.now(), "Tú");
            msg.setUserColorIndex(3);
            List<MessageListItem> ml = new ArrayList<>(mensajes.getItems());
            ml.add(msg);
            mensajes.setItems(ml);
        });
        input.setWidthFull();

        VerticalLayout conversacion = new VerticalLayout();
        conversacion.addClassNames(Flex.AUTO, Overflow.HIDDEN);
        conversacion.add(mensajes, input);

        // Presentación global
        add(conversacion, lateral);
        setSizeFull();
        expand(mensajes);

        // Cambiar de conversacion
        tabs.addSelectedChangeListener(event -> {
            actual = ((ChatTab) event.getSelectedTab()).chat;
            actual.resetearNoLeidos();
        });
    }

    public static class ChatTab extends Tab {
        final Chat chat;

        public ChatTab(Chat chat) {
            this.chat = chat;
            this.addClassNames(JustifyContent.BETWEEN);
            this.add(new Span(chat.nombre), chat.badge);
        }
    }

    public static class Chat {
        String nombre;
        int no_leidos;
        Span badge;

        Chat(String nombre, int no_leidos) {
            this.nombre = nombre;
            this.no_leidos = no_leidos;

            this.badge = new Span();
            this.badge.getElement().getThemeList().add("badge small contrast");
            actualizarBadge();
        }

        public void resetearNoLeidos() {
            no_leidos = 0;
            actualizarBadge();
        }

        public void nuevoMensaje() {
            no_leidos++;
            actualizarBadge();
        }

        void actualizarBadge() {
            badge.setText(String.valueOf(no_leidos));
            badge.setVisible(no_leidos != 0);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        Page page = attachEvent.getUI().getPage();
        page.retrieveExtendedClientDetails(details -> {
            setMobile(details.getWindowInnerWidth() < 740);
        });
        page.addBrowserWindowResizeListener(e -> {
            setMobile(e.getWidth() < 740);
        });
    }

    private void setMobile(boolean mobile) {
        tabs.setOrientation(mobile ? Orientation.HORIZONTAL : Orientation.VERTICAL);
    }
}
