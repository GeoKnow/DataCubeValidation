package rs.pupin.jpo.validation;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import rs.pupin.jpo.validation.gui.ValidationComponent;

@Theme("validation")
@SuppressWarnings("serial")
public class MyVaadinUI extends UI implements ClientConnector.DetachListener
{

    @Override
    public void detach(DetachEvent event) {
        System.out.println("Detach called!");
    }

    @WebServlet(value = { "/*", "/VAADIN/*" }, asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "rs.pupin.jpo.validation.AppWidgetSet", heartbeatInterval = 120)
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {
        addDetachListener(this);
        String g = request.getParameter("graph");
        String e = request.getParameter("endpoint");
        String o = request.getParameter("ontowiki");
//        final String graph = (g!=null)?g:"http://validation-test/ic-4-5/";
        final String graph = (g!=null)?g:"http://demo/reg-dev-polygons2/";
        final String endpoint = (e!=null)?e:"http://geoknow.imp.bg.ac.rs/sparql";
        final String owUrl = (o!=null)?o:null;
        
        final Repository repository = new SPARQLRepository(endpoint);
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            Logger.getLogger(MyVaadinUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//        final VerticalLayout layout = new VerticalLayout();
//        layout.setMargin(true);
//        setContent(layout);
//        
//        Button button = new Button("Click Me");
//        button.addClickListener(new Button.ClickListener() {
//            public void buttonClick(ClickEvent event) {
//                layout.addComponent(new Label("Thank you for clicking"));
//                Notification.show("Graph: " + graph);
//            }
//        });
//        layout.addComponent(button);
        
        setContent(new ValidationComponent(repository, endpoint, graph, owUrl));
    }

}
