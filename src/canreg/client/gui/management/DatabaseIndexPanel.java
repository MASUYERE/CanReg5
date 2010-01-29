package canreg.client.gui.management;

import canreg.common.DatabaseElement;
import canreg.common.DatabaseIndexesListElement;
import java.awt.Color;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class DatabaseIndexPanel extends DatabaseElementsPanel {

    @Action
    public void addAction() {
        add(new DatabaseIndexesListElement("New Index"));
    }

    public boolean removable(DatabaseElement dbe) {
        return true;
    }

    @Override
    public boolean visible(DatabaseElement element) {
        return true;
    }

    @Override
    public Color colorize(DatabaseElement element) {
        return null;
    }
}
