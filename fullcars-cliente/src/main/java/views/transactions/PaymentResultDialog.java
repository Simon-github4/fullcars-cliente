package views.transactions;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import dtos.MultiPaymentResponse;
import dtos.MultiPaymentResponse.PaymentSplitResponse;
import dtos.PendingSalesResponse.SalePendingInfo;
import views.components.LightTheme;

/**
 * Resultado de un pago: encabezado con el estado (saldado / parcial / con saldo a favor), montos principales
 * y detalle por metodo de pago.
 */
public class PaymentResultDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private static final String CREDIT_METHOD = "Credito a favor";

    private static final Color GREEN = new Color(46, 125, 50);
    private static final Color AMBER = new Color(217, 102, 0);
    private static final Color BLUE = LightTheme.COLOR_AZUL_FIRME;
    private static final Color PURPLE = new Color(106, 27, 154);
    private static final Color TEXT = new Color(33, 37, 41);
    private static final Color MUTED = new Color(108, 117, 125);
    private static final Color BACKGROUND = new Color(244, 246, 249);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(222, 226, 230);
    private static final Color INFO_BACKGROUND = new Color(232, 240, 254);

    /**
     * @param debtBeforePayment deuda de las ventas seleccionadas al confirmar (para calcular lo que resta)
     * @param paidSales         ventas seleccionadas (para mostrar su numero en vez del id interno)
     */
    public PaymentResultDialog(Window parent, MultiPaymentResponse resp, String customerName,
                               List<SalePendingInfo> paidSales, BigDecimal debtBeforePayment) {
        super(parent, "Pago registrado", ModalityType.APPLICATION_MODAL);

        JButton acceptButton = new JButton("Aceptar");
        acceptButton.addActionListener(e -> {
            if (parent != null) parent.dispose();
            dispose();
        });

        setContentPane(buildContent(resp, customerName, paidSales, debtBeforePayment, acceptButton));
        getRootPane().setDefaultButton(acceptButton);
        pack();
        setMinimumSize(new Dimension(640, getHeight()));
        setLocationRelativeTo(parent);
    }

    /** Arma el contenido del dialogo (separado del JDialog para poder renderizarlo sin mostrar la ventana). */
    public static JPanel buildContent(MultiPaymentResponse resp, String customerName, List<SalePendingInfo> paidSales,
                                      BigDecimal debtBeforePayment, JButton acceptButton) {
        BigDecimal total = nz(resp.getTotalAmount());
        BigDecimal creditUsed = nz(resp.getCreditUsed());
        BigDecimal creditGenerated = nz(resp.getCreditGenerated());
        BigDecimal remaining = debtBeforePayment == null ? BigDecimal.ZERO
                : debtBeforePayment.subtract(total).max(BigDecimal.ZERO);

        boolean generated = creditGenerated.signum() > 0;
        boolean partial = !generated && remaining.signum() > 0;

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(header(resp, customerName, partial, generated), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BACKGROUND);
        body.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));

        JPanel kpis = new JPanel(new GridLayout(1, 3, 12, 0));
        kpis.setOpaque(false);
        kpis.add(kpiCard("TOTAL APLICADO", money(total), BLUE));
        kpis.add(kpiCard("CRÉDITO USADO", money(creditUsed), creditUsed.signum() > 0 ? PURPLE : MUTED));
        if (generated)
            kpis.add(kpiCard("SALDO A FAVOR GENERADO", money(creditGenerated), GREEN));
        else
            kpis.add(kpiCard("RESTA POR CUBRIR", money(remaining), partial ? AMBER : GREEN));
        body.add(alignLeft(kpis));

        body.add(Box.createVerticalStrut(18));
        body.add(alignLeft(sectionTitle("Detalle por método de pago")));
        body.add(Box.createVerticalStrut(6));
        body.add(alignLeft(splitsPanel(resp.getSplits(), paidSales)));

        body.add(Box.createVerticalStrut(14));
        body.add(alignLeft(balanceStrip(resp)));

        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 14));
        footer.setBackground(BACKGROUND);
        LightTheme.aplicarEstiloPrimario(acceptButton);
        acceptButton.setPreferredSize(new Dimension(130, 36));
        footer.add(acceptButton);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    // ------------------------------------------------------------------------------------------ secciones

    private static JPanel header(MultiPaymentResponse resp, String customerName, boolean partial, boolean generated) {
        Color accent = partial ? AMBER : GREEN;

        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(accent);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel icon = new JLabel(new StatusIcon(accent, partial));
        icon.setVerticalAlignment(SwingConstants.TOP);
        header.add(icon, BorderLayout.WEST);

        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setOpaque(false);

        JLabel title = new JLabel(partial ? "Pago parcial registrado" : "Pago registrado");
        title.setFont(baseFont().deriveFont(Font.BOLD, 21f));
        title.setForeground(Color.WHITE);

        String status = generated ? "Las ventas quedaron saldadas y se generó saldo a favor."
                : partial ? "Quedó deuda pendiente en las ventas seleccionadas."
                : "Las ventas seleccionadas quedaron saldadas.";
        JLabel subtitle = new JLabel(status);
        subtitle.setFont(baseFont().deriveFont(14f));
        subtitle.setForeground(new Color(255, 255, 255, 235));

        List<String> meta = new ArrayList<>();
        meta.add("Pago #" + resp.getPaymentId());
        if (customerName != null && !customerName.isBlank()) meta.add(customerName);
        if (resp.getDate() != null) meta.add(resp.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        JLabel metaLabel = new JLabel(String.join("   ·   ", meta));
        metaLabel.setFont(baseFont().deriveFont(Font.BOLD, 13f));
        metaLabel.setForeground(new Color(255, 255, 255, 215));

        texts.add(title);
        texts.add(Box.createVerticalStrut(2));
        texts.add(subtitle);
        texts.add(Box.createVerticalStrut(6));
        texts.add(metaLabel);
        header.add(texts, BorderLayout.CENTER);
        return header;
    }

    private static JPanel kpiCard(String caption, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(4, 0, 0, 0, color),
                        BorderFactory.createMatteBorder(0, 1, 1, 1, BORDER)),
                BorderFactory.createEmptyBorder(10, 14, 12, 14)));

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(baseFont().deriveFont(Font.BOLD, 11f));
        captionLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(baseFont().deriveFont(Font.BOLD, 22f));
        valueLabel.setForeground(color);

        card.add(captionLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        return card;
    }

    private static JPanel splitsPanel(List<PaymentSplitResponse> splits, List<SalePendingInfo> paidSales) {
        Map<String, SalePendingInfo> salesById = new HashMap<>();
        if (paidSales != null)
            for (SalePendingInfo s : paidSales) salesById.put(String.valueOf(s.getSaleId()), s);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(CARD);
        list.setBorder(BorderFactory.createLineBorder(BORDER));

        List<PaymentSplitResponse> rows = splits != null ? splits : List.of();
        for (int i = 0; i < rows.size(); i++) {
            list.add(splitRow(rows.get(i), salesById, i < rows.size() - 1));
        }
        return list;
    }

    private static JPanel splitRow(PaymentSplitResponse split, Map<String, SalePendingInfo> salesById, boolean separator) {
        boolean isCredit = CREDIT_METHOD.equalsIgnoreCase(split.getPaymentMethod());
        Color color = isCredit ? PURPLE : TEXT;

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(CARD);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, separator ? 1 : 0, 0, BORDER),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, isCredit ? PURPLE : BLUE),
                        BorderFactory.createEmptyBorder(10, 12, 10, 14))));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel method = new JLabel(isCredit ? "Crédito a favor" : split.getPaymentMethod());
        method.setFont(baseFont().deriveFont(Font.BOLD, 15f));
        method.setForeground(color);
        left.add(method);

        String reference = split.getReference();
        if (reference != null && !reference.isBlank()) {
            JLabel ref = new JLabel(isCredit ? reference : "Ref: " + reference);
            ref.setFont(baseFont().deriveFont(13f));
            ref.setForeground(isCredit ? PURPLE : MUTED);
            left.add(Box.createVerticalStrut(2));
            left.add(ref);
        }

        List<String> sales = split.getSalesCovered() != null ? split.getSalesCovered() : List.of();
        if (!sales.isEmpty()) {
            List<String> labels = new ArrayList<>();
            for (String id : sales) labels.add(saleLabel(id, salesById.get(id)));
            JLabel covered = new JLabel((sales.size() == 1 ? "Venta " : "Ventas ") + String.join(", ", labels));
            covered.setFont(baseFont().deriveFont(13f));
            covered.setForeground(MUTED);
            left.add(Box.createVerticalStrut(2));
            left.add(covered);
        }
        row.add(left, BorderLayout.CENTER);

        JLabel amount = new JLabel(money(split.getAmount()));
        amount.setFont(baseFont().deriveFont(Font.BOLD, 17f));
        amount.setForeground(color);
        amount.setVerticalAlignment(SwingConstants.CENTER);
        row.add(amount, BorderLayout.EAST);
        return row;
    }

    private static JPanel balanceStrip(MultiPaymentResponse resp) {
        BigDecimal balance = nz(resp.getCustomerCreditBalance());

        JPanel strip = new JPanel(new BorderLayout(12, 4));
        strip.setBackground(INFO_BACKGROUND);
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(197, 215, 245)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JLabel caption = new JLabel("Saldo a favor del cliente después del pago");
        caption.setFont(baseFont().deriveFont(14f));
        caption.setForeground(TEXT);
        strip.add(caption, BorderLayout.CENTER);

        JLabel value = new JLabel(money(balance));
        value.setFont(baseFont().deriveFont(Font.BOLD, 17f));
        value.setForeground(balance.signum() > 0 ? GREEN : MUTED);
        strip.add(value, BorderLayout.EAST);

        String notes = resp.getDescription();
        if (notes != null && !notes.isBlank()) {
            JLabel notesLabel = new JLabel("Nota: " + notes);
            notesLabel.setFont(baseFont().deriveFont(Font.ITALIC, 13f));
            notesLabel.setForeground(MUTED);
            strip.add(notesLabel, BorderLayout.SOUTH);
        }
        return strip;
    }

    private static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(baseFont().deriveFont(Font.BOLD, 12f));
        label.setForeground(MUTED);
        return label;
    }

    // ------------------------------------------------------------------------------------------ helpers

    private static String saleLabel(String saleId, SalePendingInfo sale) {
        String label = "#" + saleId;
        if (sale != null && sale.getSaleNumber() != null && !sale.getSaleNumber().isBlank())
            label += " (sin. " + sale.getSaleNumber() + ")";
        return label;
    }

    private static <T extends JComponent> T alignLeft(T component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        return component;
    }

    private static Font baseFont() {
        Font f = UIManager.getFont("Label.font");
        return f != null ? f : new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    static String money(BigDecimal v) {
        NumberFormat f = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-AR"));
        f.setMinimumFractionDigits(2);
        f.setMaximumFractionDigits(2);
        return "$ " + f.format(nz(v).setScale(2, RoundingMode.HALF_UP));
    }

    /** Circulo blanco con un tilde (pago saldado) o un signo de exclamacion (pago parcial). */
    private static final class StatusIcon implements Icon {
        private static final int SIZE = 46;
        private final Color accent;
        private final boolean warning;

        StatusIcon(Color accent, boolean warning) {
            this.accent = accent;
            this.warning = warning;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, SIZE, SIZE);
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(4.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (warning) {
                g2.drawLine(x + SIZE / 2, y + 12, x + SIZE / 2, y + 27);
                g2.fillOval(x + SIZE / 2 - 3, y + 32, 6, 6);
            } else {
                g2.drawPolyline(new int[] { x + 13, x + 20, x + 33 }, new int[] { y + 24, y + 31, y + 16 }, 3);
            }
            g2.dispose();
        }

        @Override public int getIconWidth() { return SIZE; }
        @Override public int getIconHeight() { return SIZE; }
    }
}
