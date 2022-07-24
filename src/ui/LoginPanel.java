package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.*;

public class LoginPanel extends JPanel {
    private JLabel welcomeLabel;
    private JLabel aliasPrompt;
    private JLabel aliasInstructions;
    private JTextField aliasField;
    private JLabel hitReturnNotice;
    private JLabel badAliasWarning;
    private BoxLayout boxLayout;

    public LoginPanel() {
        welcomeLabel = new JLabel();
        welcomeLabel.setText("Welcome to Chatter.");
        welcomeLabel.setFont(new Font("Serif", Font.PLAIN, 60));
        welcomeLabel.setVisible(true);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        aliasPrompt = new JLabel();
        aliasPrompt.setText("Please enter an alias below:");
        aliasPrompt.setFont(new Font("Serif", Font.BOLD, 22));
        aliasPrompt.setVisible(true);
        aliasPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        aliasInstructions = new JLabel();
        aliasInstructions.setText("(must contain between 2-16 characters, consisting only of letters and numbers)");
        aliasInstructions.setFont(new Font("Serif", Font.ITALIC, 12));
        aliasInstructions.setVisible(true);
        aliasInstructions.setAlignmentX(Component.CENTER_ALIGNMENT);

        aliasField = new JTextField(20);
        aliasField.setVisible(true);
        aliasField.setAlignmentX(Component.CENTER_ALIGNMENT);
        aliasField.setFont(new Font("Serif", Font.PLAIN, 18));
        aliasField.setMaximumSize(aliasField.getPreferredSize());
        

        hitReturnNotice = new JLabel();
        hitReturnNotice.setText("Press \"Return\" when done.");
        hitReturnNotice.setFont(new Font("Serif", Font.PLAIN, 14));
        hitReturnNotice.setVisible(true);
        hitReturnNotice.setAlignmentX(Component.CENTER_ALIGNMENT);

        badAliasWarning = new JLabel();
        badAliasWarning.setText("Alias must contain only letters and numbers & be between 2-16 characters long!");
        badAliasWarning.setForeground(Color.RED);
        badAliasWarning.setFont(new Font("Serif", Font.PLAIN, 14));
        badAliasWarning.setVisible(true);
        badAliasWarning.setAlignmentX(Component.CENTER_ALIGNMENT);

        // setting up the box layout.
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(welcomeLabel);
        this.add(aliasPrompt);
        this.add(aliasInstructions);
        this.add(aliasField);
        this.add(hitReturnNotice);
        this.add(badAliasWarning);

    }
}
