package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import InitClass.Initializr;
import data.service.LoginService;
import data.service.LoginService.User;

public class LoginView extends JFrame {

	private final LoginService loginService;
	private final JTextField usernameField;
	private final JPasswordField passwordField;
	private final JLabel messageLabel;
	private final JButton loginButton;

	public LoginView() {
		this.loginService = new LoginService();

		setTitle("Login del Sistema");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(380, 240);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(10, 10));
		setVisible(true);
		
		// Panel central con formulario
		JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 12));
		formPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 15, 25));

		JLabel userLabel = new JLabel("Usuario:");
		usernameField = new JTextField();
		JLabel passLabel = new JLabel("Contraseña:");
		passwordField = new JPasswordField();

		// Añadir KeyListeners para moverse con Enter
		usernameField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					passwordField.requestFocus();
				}
			}
		});

		passwordField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					loginButton.doClick();
				}
			}
		});

		formPanel.add(userLabel);
		formPanel.add(usernameField);
		formPanel.add(passLabel);
		formPanel.add(passwordField);

		// Panel inferior con botones más grandes
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		loginButton = new JButton("Ingresar");
		JButton exitButton = new JButton("Salir");

		Dimension buttonSize = new Dimension(120, 35);
		loginButton.setPreferredSize(buttonSize);
		exitButton.setPreferredSize(buttonSize);

		loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
		exitButton.setFont(new Font("SansSerif", Font.BOLD, 14));

		loginButton.addActionListener(this::loginAction);
		exitButton.addActionListener(e -> System.exit(0));

		buttonPanel.add(loginButton);
		buttonPanel.add(exitButton);

		// Mensaje de estado
		messageLabel = new JLabel(" ");
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		messageLabel.setForeground(Color.RED);
		messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

		// Ensamblado
		add(messageLabel, BorderLayout.NORTH);
		add(formPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void loginAction(ActionEvent e) {
		String username = usernameField.getText().trim();
		String password = new String(passwordField.getPassword()).trim();

		if (username.isEmpty() || password.isEmpty()) {
			showMessage("Complete todos los campos");
			return;
		}

		loginService.login(username, password).ifPresentOrElse(this::loginSuccess,
				() -> showMessage("Usuario o contraseña incorrectos"));
	}

	private void loginSuccess(User user) {
		//showMessage("Bienvenido " + user.getUsername());

		SwingUtilities.invokeLater(() -> {
			dispose();
			Initializr.launch(user);
		});
	}

	private void showMessage(String msg) {
		messageLabel.setText(msg);
	}
}
