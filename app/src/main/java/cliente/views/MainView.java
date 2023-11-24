package cliente.views;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;

import cliente.App;
import cliente.ClienteImpl;
import cliente.Mensaje;
import cliente.security.SecurityService;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "")
@PageTitle("Mess")
public class MainView extends HorizontalLayout {
    ClienteImpl cliente;
    Map<String, Chat> chats;
    Chat actual;
    TextField search;
    Tabs tabs;
    HiloActualizaciones thread;

    public MainView(SecurityService security) {
        // Esperamos a que la aplicación incialice el cliente y se haya conectado
        while (cliente == null) {
            cliente = App.get();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        // Conectamos el cliente al servidor
        String user = security.getAuthenticatedUser().getUsername();
        if (!cliente.estaConectado()) {
            try {
                cliente.iniciarSesion(user);
            } catch (RemoteException e) {
                add(new H3("Error al iniciar sesión"));
                return;
            }
        }

        // Estilo
        addClassNames("chat-view", Width.FULL, Display.FLEX, Flex.AUTO);

        // Chats
        chats = new HashMap<>();
        for (String u : cliente.getClientes().keySet()) {
            chats.put(u, new Chat(u));
        }

        // Barra de chats
        H3 titulo_lateral = new H3("Amistades");

        search = new TextField();
        search.setLabel("Buscar...");
        search.setClearButtonVisible(true);
        search.setPrefixComponent(VaadinIcon.SEARCH.create());

        tabs = new Tabs();
        for (Chat chat : chats.values()) {
            tabs.add(new ChatTab(chat));
        }
        tabs.setOrientation(Orientation.VERTICAL);
        tabs.addClassNames(Flex.GROW, Flex.SHRINK, Overflow.HIDDEN);

        Button logout = new Button(user, e -> security.logout());

        Aside lateral = new Aside();
        lateral.addClassNames(Display.FLEX, FlexDirection.COLUMN, Flex.GROW_NONE, Flex.SHRINK_NONE,
                Background.CONTRAST_5, Padding.MEDIUM);
        lateral.add(titulo_lateral, search, tabs, logout);
        lateral.setWidth("18rem");

        // Contenido
        MessageInput input = new MessageInput();
        input.addSubmitListener(submitEvent -> {
            String msg = submitEvent.getValue();
            if (msg.isEmpty())
                return;
            actual.nuevoMensaje(new Mensaje(msg));
        });
        input.setWidthFull();
        input.addClassNames(Padding.NONE);

        VerticalLayout conversacion = new VerticalLayout();
        conversacion.addClassNames(Flex.AUTO, Overflow.HIDDEN);

        // Presentación global
        add(conversacion, lateral);
        setSizeFull();

        // Cambiar de conversacion
        tabs.addSelectedChangeListener(event -> {
            actual = ((ChatTab) event.getSelectedTab()).chat;
            conversacion.removeAll();
            conversacion.add(actual.getList(), input);
        });

        if (chats.size() == 0) {
            actual = new Chat("No hay chats");
        } else {
            actual = chats.values().iterator().next();
        }
        conversacion.removeAll();
        conversacion.add(actual.getList(), input);
    }

    class ChatTab extends Tab {
        final Chat chat;

        public ChatTab(Chat chat) {
            this.chat = chat;
            this.addClassNames(JustifyContent.BETWEEN);
            this.add(new Span(chat.user), chat.badge);
        }
    }

    class Chat {
        String user;
        List<Mensaje> mensajes;
        int no_leidos;
        MessageList html;
        Span badge;

        Chat(String user) {
            this.user = user;
            no_leidos = 0;

            badge = new Span();
            badge.getElement().getThemeList().add("badge small contrast");

            html = new MessageList();
            html.setWidthFull();
            expand(html);

            actualizarMensajes();
        }

        public void nuevoMensaje(Mensaje msg) {
            try {
                cliente.enviar(user, msg);
            } catch (RemoteException e) {
                System.out.println("error enviando mensaje " + e.getMessage());
            }
            actualizarMensajes();
        }

        public MessageList getList() {
            return html;
        }

        public void actualizarMensajes() {
            if (cliente.getMensajes().containsKey(user)) {
                mensajes = cliente.getMensajes().get(user);
            } else {
                mensajes = new ArrayList<>();
            }

            List<MessageListItem> ml = new ArrayList<>();
            for (Mensaje m : mensajes) {
                ml.add(new MessageListItem(m.toString(), m.instant(), m.getUsuario()));
            }
            ml.sort((a, b) -> a.getTime().compareTo(b.getTime()));
            html.setItems(ml);

            badge.setText(String.valueOf(no_leidos));
            badge.setVisible(no_leidos != 0);
        }
    }

    public class HiloActualizaciones extends Thread {
        private final UI ui;
        private final MainView view;

        public HiloActualizaciones(UI ui, MainView view) {
            this.ui = ui;
            this.view = view;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(1000);
                    ui.access(() -> {
                        // Actualizar clientes
                        for (String c : view.cliente.getClientes().keySet()) {
                            if (!view.chats.containsKey(c)) {
                                view.chats.put(c, new Chat(c));
                                view.tabs.add(new ChatTab(view.chats.get(c)));
                            }
                        }

                        // Eliminar clientes
                        List<String> eliminar = new ArrayList<>();
                        for (String c : view.chats.keySet()) {
                            if (!view.cliente.getClientes().containsKey(c)) {
                                eliminar.add(c);
                            }
                        }
                        for (String c : eliminar) {
                            view.chats.remove(c);
                        }

                        // TODO: Eliminar de la barra lateral

                        // Actualizar mensajes
                        // FIX: No se actualiza la ui
                        for (Chat c : view.chats.values()) {
                            c.actualizarMensajes();
                        }
                    });
                }
            } catch (InterruptedException e) {
            }
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

        // Hilo para obtener actualizaciones
        thread = new HiloActualizaciones(attachEvent.getUI(), this);
        thread.start();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        thread.interrupt();
        thread = null;
    }

    private void setMobile(boolean mobile) {
        tabs.setOrientation(mobile ? Orientation.HORIZONTAL : Orientation.VERTICAL);
    }
}
