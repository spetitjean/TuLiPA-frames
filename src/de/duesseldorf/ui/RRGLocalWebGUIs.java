package de.duesseldorf.ui;

import de.duesseldorf.ui.webgui.RRGLocalWebGUI;

import java.util.LinkedList;
import java.util.List;

public class RRGLocalWebGUIs {
    private static List<RRGLocalWebGUI> localWebGUIs = new LinkedList<RRGLocalWebGUI>();


    public static void addLocalWebGUI(RRGLocalWebGUI localWebGUI) {
        localWebGUIs.add(localWebGUI);
    }

    public static void stopAllLocalWebGUIServers() {
        for (RRGLocalWebGUI localWebGUI : localWebGUIs) {
            localWebGUI.getServer().stop(0);
        }
    }

    public static int numberOfRunningGUIs(){
        return localWebGUIs.size();
    }
}
