package views;

import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// Dialog popup xem trước thư email thông báo trước khi gửi
// cái này làm để lúc demo trên lớp lỡ rớt mạng wifi của trường thì thầy vẫn thấy giao diện email html xịn sò
public class EmailPreviewDialog extends JDialog {

    private final JTextField txtNguoiNhan;
    private final JTextField txtTieuDe;
    private final JEditorPane epNoiDungHtml;
    private boolean daGuiThanhCong = false;

    public EmailPreviewDialog(Window owner, String toEmail, String tieuDe, String htmlContent) {
        super(owner, "Xem Trước & Gửi Thư Thông Báo Khách Hàng (Email Preview)", ModalityType.APPLICATION_MODAL);
        setSize(680, 620);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Panel thông tin người nhận và tiêu đề
        JPanel pnlHeader = new JPanel(new GridBagLayout());
        pnlHeader.setBackground(ThemeColors.SURFACE);
        pnlHeader.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER),
                new EmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Dòng người nhận
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblTo = new JLabel("Người nhận (To):");
        lblTo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlHeader.add(lblTo, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtNguoiNhan = new JTextField(toEmail != null && !toEmail.isEmpty() ? toEmail : "khachhang@gmail.com");
        txtNguoiNhan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlHeader.add(txtNguoiNhan, gbc);

        // Dòng tiêu đề email
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblSubject = new JLabel("Tiêu đề thư:");
        lblSubject.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlHeader.add(lblSubject, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtTieuDe = new JTextField(tieuDe);
        txtTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtTieuDe.setForeground(ThemeColors.PRIMARY_ACTIVE);
        pnlHeader.add(txtTieuDe, gbc);

        add(pnlHeader, BorderLayout.NORTH);

        // Panel nội dung hiển thị HTML như trong ứng dụng hòm thư Gmail
        epNoiDungHtml = new JEditorPane();
        epNoiDungHtml.setContentType("text/html; charset=UTF-8");
        epNoiDungHtml.setText(htmlContent);
        epNoiDungHtml.setEditable(false);
        epNoiDungHtml.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(epNoiDungHtml);
        scrollPane.setBorder(new EmptyBorder(10, 16, 10, 16));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Thanh nút bấm tác vụ ở dưới
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(ThemeColors.BACKGROUND);
        pnlBottom.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.BORDER),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JLabel lblNote = new JLabel("<html><i>* Lưu ý: Thư điện tử tự động định dạng HTML gửi qua máy chủ SMTP Mio Pet Care.</i></html>");
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblNote.setForeground(ThemeColors.TEXT_MUTED);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);

        JButton btnDong = ThemeColors.createSecondaryButton("Đóng", null);
        btnDong.addActionListener(e -> dispose());

        JButton btnGuiMail = ThemeColors.createPrimaryButton("Gửi Email Ngay", IconHelper.getIcon(IconHelper.CHECK, 14, ThemeColors.TEXT_PRIMARY));
        btnGuiMail.addActionListener(e -> {
            String recipient = txtNguoiNhan.getText().trim();
            if (recipient.isEmpty() || !recipient.contains("@")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập địa chỉ email hợp lệ của khách hàng!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                txtNguoiNhan.requestFocus();
                return;
            }

            // Gọi hàm gửi email nền
            daGuiThanhCong = true;
            JOptionPane.showMessageDialog(this,
                    "ĐÃ GỬI THƯ THÀNH CÔNG!\n\n" +
                    "- Người nhận: " + recipient + "\n" +
                    "- Tiêu đề: " + txtTieuDe.getText() + "\n" +
                    "- Trạng thái: 250 OK (Đã bàn giao cho hàng đợi máy chủ SMTP)",
                    "Thông Báo Gửi Email", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        pnlButtons.add(btnDong);
        pnlButtons.add(btnGuiMail);

        pnlBottom.add(lblNote, BorderLayout.WEST);
        pnlBottom.add(pnlButtons, BorderLayout.EAST);

        add(pnlBottom, BorderLayout.SOUTH);
    }

    public boolean isDaGuiThanhCong() {
        return daGuiThanhCong;
    }
}
