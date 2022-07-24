package ui;

import javax.swing.*;

public class MainWindow {
    
    private JFrame frame;

    public MainWindow(JPanel panel) {
        frame = new JFrame("Chatter");
        frame.setContentPane(panel);
        frame.setSize(500,300);
        frame.setLocationRelativeTo(null); // centers the component
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /*
     * HelloWorldDisplay displayPanel = new HelloWorldDisplay();
      JButton okButton = new JButton("OK");
      ButtonHandler listener = new ButtonHandler();
      okButton.addActionListener(listener);

      JPanel content = new JPanel();
      content.setLayout(new BorderLayout());
      content.add(displayPanel, BorderLayout.CENTER);
      content.add(okButton, BorderLayout.SOUTH);

      JFrame window = new JFrame("GUI Test");
      window.setContentPane(content);
      window.setSize(250,100);
      window.setLocation(100,100);
      window.setVisible(true);
     */
}
