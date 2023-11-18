package cliente;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends VerticalLayout {
    ClienteImpl cliente;

    public MainView() {
        addClassName("centered-content");

        // Pantalla de carga
        add(new Paragraph("cargando..."));

        // Esperamos a que la aplicación incialice el cliente y se haya conectado
        while (cliente == null) {
            cliente = App.get();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        removeAll();

        add(new H1("messi"),
                new Paragraph("Nombre: " + cliente.ip + ":" + cliente.puerto),
                new Paragraph("holaa"));

        add(new Paragraph("adios"));
    }
}
