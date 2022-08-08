package ui;

import javax.swing.DefaultListModel;
import java.util.ArrayList;

public class MyListModel extends DefaultListModel<String> {

    private ArrayList<String> elements;

    MyListModel() {
        elements = new ArrayList<String>();
    }

    public int getSize() {
        return elements.size();
    }

    public String getElementAt(int i) {
        return elements.get(i);
    }

    public void addElement(String elt) {
        elements.add(elt);
    }

    public void clear() {
        elements.clear();
    }

}
