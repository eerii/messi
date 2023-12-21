package cliente.views;

import static shared.Utils.*;


import cliente.ClienteImpl;
import cliente.security.SecurityService;
import shared.Mensaje;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.time.Instant;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.messages.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.Orientation;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
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

    public MainView(SecurityService security) {
        // Esperamos a que la aplicación incialice el cliente y se haya conectado
        while (cliente == null) {
            try {
                cliente = ClienteImpl.get();
            } catch (RemoteException e) {
                add(new H3("Error al conectar al servidor"));
            }
        }

        // Conectamos el cliente al servidor
        /*String user = security.getAuthenticatedUser().getUsername();
        if (!cliente.estaConectado()) {
            try {
                cliente.iniciarSesion(user, "TODO");
            } catch (RemoteException e) {
                add(new H3("Error al iniciar sesión"));
                return;
            }
        }*/

        // Estilo
        addClassNames("chat-view", Width.FULL, Display.FLEX, Flex.AUTO);

        // Barra de chats
        H3 titulo_lateral = new H3("Amistades");

        search = new TextField();
        search.setLabel("Buscar...");
        search.setClearButtonVisible(true);
        search.setPrefixComponent(VaadinIcon.SEARCH.create());

        tabs = new Tabs();
        // <theme-editor-local-classname>
        tabs.addClassName("main-view-tabs-1");
        tabs.setOrientation(Orientation.VERTICAL);
        tabs.addClassNames(Flex.GROW, Flex.SHRINK, Overflow.HIDDEN);

        Button logout = new Button("Cerrar sesión", e -> {
            security.logout();
            cliente.setUI(null, null);
            try {
                cliente.cerrarSesion();
            } catch (RemoteException _e) {
            }
            cliente = null;
        });

        Aside lateral = new Aside();
        lateral.addClassNames(Display.FLEX, FlexDirection.COLUMN, Flex.GROW_NONE, Flex.SHRINK_NONE,
                Background.CONTRAST_5, Padding.MEDIUM);
        lateral.add(titulo_lateral, search, tabs, logout);
        lateral.setWidth("18rem");

        // Contenido
        MessageInput input = new MessageInput();
        input.addClassName("main-view-message-input-1");
        input.addSubmitListener(submitEvent -> {
            String msg = submitEvent.getValue();
            if (actual == null || chats == null || msg.isEmpty())
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
            conversacion.add(actual.getTitulo(), actual.getList(), input);
        });

        if (chats == null || chats.size() == 0) {
            actual = new Chat("No hay chats");
        } else {
            actual = chats.values().iterator().next();
        }
        conversacion.removeAll();
        conversacion.add(actual.getTitulo(), actual.getList(), input);
    }

    class MensajeDesencriptado {
        String msg;
        String user;
        Instant hora;

        public MensajeDesencriptado(Mensaje msg, SecretKey key) {
            this.user = msg.getUsuario();
            this.hora = msg.instant();

            if (msg.encriptado()) {
                try {
                    this.msg = msg.desencriptar(key);
                } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                    this.msg = "Error al desencriptar";
                }
            } else {
                this.msg = msg.toString();
            }
        }
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
        List<MensajeDesencriptado> mensajes;
        int no_leidos;
        HorizontalLayout titulo;
        Icon seguro;
        Span emoticonos;
        MessageList html;
        Span badge;

        Chat(String user) {
            this.user = user;
            no_leidos = 0;

            badge = new Span();
            badge.getElement().getThemeList().add("badge small contrast");

            titulo = new HorizontalLayout();
            seguro = VaadinIcon.LOCK.create();
            seguro.setSize("1em");
            emoticonos = new Span(VaadinIcon.UNLOCK.create());
            titulo.add(new H3(user), emoticonos);
            titulo.addClassNames(Display.FLEX, JustifyContent.BETWEEN, Margin.NONE);
            titulo.setWidthFull();

            html = new MessageList();
            html.setWidthFull();
            expand(html);
        }

        public void nuevoMensaje(Mensaje msg) {
                cliente.enviar(user, msg);
        }

        public MessageList getList() {
            return html;
        }

        public HorizontalLayout getTitulo() {
            return titulo;
        }

        public void actualizarMensajes(List<MensajeDesencriptado> mensajes, String emoji) {
            mensajes.sort((a, b) -> a.hora.compareTo(b.hora));
            this.mensajes = mensajes;

            emoticonos.removeAll();
            emoticonos.add(emoji);

            List<MessageListItem> ml = new ArrayList<>();
            for (MensajeDesencriptado m : mensajes) {
                ml.add(new MessageListItem(m.msg, m.hora, m.user));
            }
            html.setItems(ml);

            badge.setText(String.valueOf(no_leidos));
            badge.setVisible(no_leidos != 0);
        }
    }

    public void actualizarClientes(Set<String> clientes) {
        if (chats == null)
            chats = new HashMap<>();

        // Añadir clientes
        for (String c : clientes) {
            if (!chats.containsKey(c)) {
                chats.put(c, new Chat(c));
                tabs.add(new ChatTab(chats.get(c)));
            }
        }

        // Eliminar clientes
        List<String> eliminar = new ArrayList<>();
        for (String c : chats.keySet()) {
            if (!clientes.contains(c)) {
                eliminar.add(c);
            }
        }
        for (String c : eliminar) {
            chats.remove(c);
        }
    }

    public void actualizarMensajes(String user, List<Mensaje> mensajes, SecretKey key) {
        if (chats == null || !chats.containsKey(user))
            return;

        List<MensajeDesencriptado> ml = new ArrayList<>();
        for (Mensaje m : mensajes) {
            ml.add(new MensajeDesencriptado(m, key));
        }

        String emoji = emojiFromHex(bytesToHex(key.getEncoded()));

        chats.get(user).actualizarMensajes(ml, emoji);
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

        if (cliente != null)
            cliente.setUI(attachEvent.getUI(), this);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (cliente != null) {
            try {
                cliente.cerrarSesion();
            } catch (Exception e) {
            }
            cliente = null;
        }
    }

    private void setMobile(boolean mobile) {
        if (tabs != null)
            tabs.setOrientation(mobile ? Orientation.HORIZONTAL : Orientation.VERTICAL);
    }
}
