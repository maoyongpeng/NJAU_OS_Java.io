package MainApplicationFolder;

import MainApplicationFolder.GUI_OpFolder.GetNewSwingForm;
import MainApplicationFolder.GUI_OpFolder.UI;

public class Main {
    public static void main(String[] args) {
        try {
            UI.LoginMainForm();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
