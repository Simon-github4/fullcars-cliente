package views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.client.entities.Customer;
import model.client.entities.Pay;
import model.client.entities.Sale;
import raven.datetime.DatePicker;
import raven.datetime.DatePicker.DateSelectionMode;

public class PayForm extends JDialog {

    private JTextField amountField;
    private DatePicker datePicker;
    private JComboBox<String> payMethodCombo;
    private JButton saveButton;
    private JButton cancelButton;

    private Pay createdPay; // El pago que se crea al cerrar el diálogo
    private Customer customer;
    private Sale sale;

    public PayForm(Frame owner, Customer customer, Sale sale) {
        super(owner, "Nuevo Pago", true);
        this.customer = customer;
        this.sale = sale;

        initComponents();
        layoutComponents();
        attachListeners();

        setMinimumSize(new Dimension(600,400)); // tamaño mínimo
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        amountField = new JTextField(15); // campo más grande
        datePicker = new DatePicker();
        datePicker.setDateSelectionMode(DateSelectionMode.SINGLE_DATE_SELECTED);
        datePicker.setSelectedDate(LocalDate.now());
        datePicker.setBackground(Color.DARK_GRAY);
        payMethodCombo = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Transferencia"});
        saveButton = new JButton("Guardar");
        cancelButton = new JButton("Cancelar");
        
        payMethodCombo.setPreferredSize(new Dimension(200, 25));
        saveButton.setPreferredSize(new Dimension(200, 25));
        cancelButton.setPreferredSize(new Dimension(200, 25));

    }

    private void layoutComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10); // más espacio
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Monto:"), gbc);
        gbc.gridx = 1; panel.add(amountField, gbc);

        JFormattedTextField tf = new JFormattedTextField(15);
        datePicker.setEditor(tf);
        tf.setPreferredSize(new Dimension(200, 25));
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Fecha (dd-mm-yyyy):"), gbc);
        gbc.gridx = 1; panel.add(tf, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Método de Pago:"), gbc);
        gbc.gridx = 1; panel.add(payMethodCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);
        panel.add(buttonsPanel, gbc);

        getContentPane().add(panel);
    }

    private void attachListeners() {
        saveButton.addActionListener((ActionEvent e) -> onSave());
        cancelButton.addActionListener((ActionEvent e) -> onCancel());
    }

    private void onSave() {
        try {
            int amount = Integer.parseInt(amountField.getText());
            LocalDate date = datePicker.getSelectedDate();
            String method = (String) payMethodCombo.getSelectedItem();

            createdPay = new Pay();
            createdPay.setAmount(amount);
            createdPay.setDate(date);
            createdPay.setPaymentMethod(method);
            createdPay.setCustomer(customer);
            createdPay.setSale(sale);

            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Datos inválidos: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
        createdPay = null;
        dispose();
    }

    public Pay showDialog() {
        setVisible(true);
        return createdPay;
    }
    
}
