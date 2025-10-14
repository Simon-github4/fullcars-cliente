package views.transactions;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DialogTallerPatente extends JDialog {

    private JTextField tallerField;
    private JTextField patenteField;
    private JButton aceptarButton;

    private String taller;
    private String patente;

    public DialogTallerPatente(Frame owner) {
        super(owner, "Ingresar Taller y Patente", true);
        initComponents();
        layoutComponents();
        attachListeners();

        // Quita el botón de cerrar (X)
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Bloquea ALT+F4 y otros cierres
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // no hace nada
            }
        });

        setMinimumSize(new Dimension(400, 250));
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        tallerField = new JTextField(20);
        patenteField = new JTextField(20);

        aceptarButton = new JButton("Aceptar");
        aceptarButton.setPreferredSize(new Dimension(150, 30));
    }

    private void layoutComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Taller:"), gbc);
        gbc.gridx = 1; panel.add(tallerField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Patente:"), gbc);
        gbc.gridx = 1; panel.add(patenteField, gbc);

        // Botón centrado
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(aceptarButton);
        panel.add(buttonPanel, gbc);

        getContentPane().add(panel);
    }

    private void attachListeners() {
        aceptarButton.addActionListener((ActionEvent e) -> onAceptar());
    }

    private void onAceptar() {
        String t = tallerField.getText().trim();
        String p = patenteField.getText().trim();

        if (t.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Debe ingresar ambos valores: Taller y Patente.",
                "Campos incompletos",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        taller = t;
        patente = p;
        dispose(); // cerrar diálogo
    }

    public String getTaller() {
        return taller;
    }

    public String getPatente() {
        return patente;
    }

    // Muestra el diálogo y devuelve true si se completó correctamente
    public boolean showDialog() {
        setVisible(true);
        return taller != null && patente != null;
    }

}