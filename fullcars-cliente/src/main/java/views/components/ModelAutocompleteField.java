package views.components;


import java.awt.BorderLayout;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import model.client.entities.Model;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class ModelAutocompleteField extends JPanel {
    private final JTextField textField;
    private final JPopupMenu suggestionsPopup;
    private List<Model> allModels;
    private Model selectedModel; // lo que realmente se elige
    private JList<Model> suggestionList;

    public ModelAutocompleteField(List<Model> models) {
        this.allModels = models;
        this.setLayout(new BorderLayout());

        textField = new JTextField();
        suggestionsPopup = new JPopupMenu();
        suggestionsPopup.setFocusable(false);

        // Listener para actualizar sugerencias mientras se escribe
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { showSuggestions(); }
            @Override
            public void removeUpdate(DocumentEvent e) { showSuggestions(); }
            @Override
            public void changedUpdate(DocumentEvent e) { showSuggestions(); }
        });

        // Listener para flechas ↑↓ y Enter
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (suggestionsPopup.isVisible() && suggestionList != null) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        int nextIndex = Math.min(suggestionList.getSelectedIndex() + 1,
                                                 suggestionList.getModel().getSize() - 1);
                        suggestionList.setSelectedIndex(nextIndex);
                        suggestionList.ensureIndexIsVisible(nextIndex);
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        int prevIndex = Math.max(suggestionList.getSelectedIndex() - 1, 0);
                        suggestionList.setSelectedIndex(prevIndex);
                        suggestionList.ensureIndexIsVisible(prevIndex);
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        Model chosen = suggestionList.getSelectedValue();
                        if (chosen != null) {
                            applySelection(chosen);
                        }
                        e.consume();
                    }
                }
            }
        });

        this.add(textField, BorderLayout.CENTER);
    }

    private void showSuggestions() {
        String input = textField.getText().trim();
        suggestionsPopup.setVisible(false);
        suggestionsPopup.removeAll();

        if (input.isEmpty()) {
            return;
        }

        List<Model> matches = allModels.stream()
                .filter(m -> m.getName().toLowerCase().contains(input.toLowerCase()))
                .limit(10)
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            return;
        }

        suggestionList = new JList<>(new Vector<>(matches));
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setVisibleRowCount(Math.min(8, matches.size()));

        // Doble click con mouse
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Model chosen = suggestionList.getSelectedValue();
                    if (chosen != null) {
                        applySelection(chosen);
                    }
                }
            }
        });

        suggestionsPopup.add(new JScrollPane(suggestionList));
        suggestionsPopup.show(textField, 0, textField.getHeight());
        textField.requestFocusInWindow();
    }

    private void applySelection(Model model) {
        textField.setText(model.getName());
        selectedModel = model;
        suggestionsPopup.setVisible(false);
    }

    public Model getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(Model model) {
        this.selectedModel = model;
        if (model != null) {
            textField.setText(model.getName());
        } else {
            textField.setText("");
        }
    }
    
    public void setModels(List<Model> models) {
        this.allModels = models;       // reemplaza la lista
        this.selectedModel = null;     // opcional: limpiar selección actual
        textField.setText("");         // opcional: limpiar el campo
        suggestionsPopup.setVisible(false); // cerrar popup si está abierto
    }

}
