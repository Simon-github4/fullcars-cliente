package views.components;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.function.Function;

public class AutocompleteField<T> extends JPanel {
    private final JTextField textField;
    private final JPopupMenu suggestionsPopup;
    private List<T> items;
    private T selectedItem;
    private final Function<T, String> displayFunction;
    private JList<T> suggestionList;
    private boolean programmaticChange = false;

    public AutocompleteField(int cols) {
    	this();
    	textField.setColumns(cols);
    }
    public AutocompleteField() {
        this.items = null;
        this.displayFunction = Object::toString;

        this.setLayout(new BorderLayout());
        textField = new JTextField();
        suggestionsPopup = new JPopupMenu();
        suggestionsPopup.setFocusable(false);

        // DocumentListener
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { handleTextChanged(); }
            @Override
            public void removeUpdate(DocumentEvent e) { handleTextChanged(); }
            @Override
            public void changedUpdate(DocumentEvent e) { handleTextChanged(); }

            private void handleTextChanged() {
                if (programmaticChange) return;

                String input = textField.getText().trim();
                if (selectedItem != null && !displayFunction.apply(selectedItem).equals(input)) {
                    selectedItem = null;
                }
                showSuggestions();
            }
        });

        // Navegación con teclado
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (suggestionsPopup.isVisible() && suggestionList != null) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        int next = Math.min(suggestionList.getSelectedIndex() + 1,
                                suggestionList.getModel().getSize() - 1);
                        suggestionList.setSelectedIndex(next);
                        suggestionList.ensureIndexIsVisible(next);
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        int prev = Math.max(suggestionList.getSelectedIndex() - 1, 0);
                        suggestionList.setSelectedIndex(prev);
                        suggestionList.ensureIndexIsVisible(prev);
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        T chosen = suggestionList.getSelectedValue();
                        if (chosen != null) applySelection(chosen);
                        e.consume();
                    }
                }
            }
        });

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(() -> showSuggestions());
            }

            @Override
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if (focusOwner == null || !suggestionsPopup.isAncestorOf(focusOwner)) {
                        suggestionsPopup.setVisible(false);
                    }
                });
            }
        });

        this.add(textField, BorderLayout.CENTER);
    }

    private void showSuggestions() {
        String input = textField.getText().trim();
        suggestionsPopup.setVisible(false);
        suggestionsPopup.removeAll();

        List<T> matches;
        if (input.isEmpty()) {
            matches = new ArrayList<>(items);
        } else {
            matches = items.stream()
                    .filter(item -> displayFunction.apply(item).toLowerCase().contains(input.toLowerCase()))
                    .limit(50)
                    .collect(Collectors.toList());
        }

        if (matches.isEmpty()) return;

        suggestionList = new JList<>(new Vector<>(matches));
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Font baseFont = textField.getFont();
        suggestionList.setFont(baseFont.deriveFont(baseFont.getSize2D() + 2f));
        int rowHeight = suggestionList.getFont().getSize() + 10; // padding
        suggestionList.setFixedCellHeight(rowHeight);

        int visibleRows = Math.min(7, matches.size());  // no más de 7
        visibleRows = Math.max(2, visibleRows);        // al menos 2
        suggestionList.setVisibleRowCount(visibleRows);

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    T chosen = suggestionList.getSelectedValue();
                    if (chosen != null) applySelection(chosen);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(suggestionList);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0)); // margen superior/inferior
        scrollPane.setPreferredSize(new Dimension(textField.getWidth(), rowHeight * visibleRows));

        suggestionsPopup.add(scrollPane);
        suggestionsPopup.show(textField, 0, textField.getHeight());
        suggestionsPopup.setPopupSize(textField.getWidth(), rowHeight * visibleRows);
        textField.requestFocusInWindow();
    }

    private void applySelection(T item) {
        programmaticChange = true;
        try {
            selectedItem = item;
            textField.setText(displayFunction.apply(item));
            suggestionsPopup.setVisible(false);
        } finally {
            programmaticChange = false;
        }
    }

    public T getSelectedItem() {
        return selectedItem;
    }

    public void setItems(List<T> newItems) {
        this.items = new ArrayList<>(newItems);
        this.selectedItem = null;
        programmaticChange = true;
        try {
            textField.setText("");
            suggestionsPopup.setVisible(false);
        } finally {
            programmaticChange = false;
        }
    }

    public JTextField getTextField() {
        return textField;
    }

    public void setSelectedItem(T item) {
        programmaticChange = true;
        try {
            this.selectedItem = item;
            if (item != null) {
                textField.setText(displayFunction.apply(item));
            } else {
                textField.setText("");
            }
        } finally {
            programmaticChange = false;
        }
    }

	public void clearSelection() {
		setSelectedItem(null); 
	}
	
	@Override
	public void setEnabled(boolean enable) {
		clearSelection();
		textField.setEnabled(enable);
	}
}
