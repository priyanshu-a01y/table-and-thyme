package restaurant;

import restaurant.ui.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            System.err.println("[STARTUP] Uncaught failure on " + thread.getName());
            error.printStackTrace();
        });
        try {
            boolean nimbusSet = false;
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    nimbusSet = true;
                    break;
                }
            }
            if (!nimbusSet) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> {
            try {
                if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Swing startup is not on the EDT");
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                loginFrame.toFront();
                System.out.println("[STARTUP] Login window created and visible on the Swing EDT.");
            } catch (Throwable error) {
                System.err.println("[STARTUP] Could not create the login window.");
                error.printStackTrace();
                JOptionPane.showMessageDialog(null, "The sign-in window could not be opened. See the application log for details.",
                        "Startup error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
