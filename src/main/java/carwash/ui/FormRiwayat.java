/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package carwash.ui;

import carwash.exception.AksesDitolakException;
import carwash.exception.InputTidakValidException;
import carwash.exception.PenyimpananGagalException;
import carwash.logic.AppContext;
import carwash.logic.LoyaltyTracker;
import carwash.logic.Rupiah;
import carwash.logic.Session;
import carwash.logic.StrukBuilder;
import carwash.logic.Validator;
import carwash.model.Customer;
import carwash.model.KoreksiLoyalitas;
import carwash.model.Role;
import carwash.model.Transaction;
import carwash.model.User;

import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author argor
 */
public class FormRiwayat extends javax.swing.JFrame {

    private final AppContext ctx = AppContext.get();
    private final Customer customer;                 // null di mode Manager
    private boolean modeKasir;
    private final List<String> kasirIds = new ArrayList<>();
    private List<Transaction> tampilTx = new ArrayList<>();
    private DefaultTableModel modelTx;
    private DefaultTableModel modelKoreksi;

    public FormRiwayat(Customer customer) {
        this.customer = customer;
        initComponents();

        // Lapis kedua: Kasir (dengan pelanggan) atau Manager
        Role role = Session.isLoggedIn() ? Session.getUser().getRole() : null;
        if (role == null || (role == Role.KASIR && customer == null)) {
            dispose();
            new FormLogin().setVisible(true);
            return;
        }
        modeKasir = (role == Role.KASIR);

        setLocationRelativeTo(null);
        pnlKoreksi.setVisible(modeKasir);
        pnlFilter.setVisible(!modeKasir);

        modelTx = buatModel("ID", "Tanggal", "Pelanggan", "Kasir", "Total", "Reward");
        tblTransaksi.setModel(modelTx);
        tblTransaksi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modelKoreksi = buatModel("Tanggal", "Pelanggan", "Kasir", "Lama", "Baru", "Alasan");
        tblKoreksi.setModel(modelKoreksi);

        if (modeKasir) {
            lblJudul.setText("Riwayat dan Koreksi - " + customer.getNama() + " (" + customer.getId() + ")");
        } else {
            lblJudul.setText("Riwayat Semua Transaksi dan Log Koreksi (hanya lihat)");
            List<String> item = new ArrayList<>();
            item.add("Semua kasir");
            for (User u : ctx.getAuth().getDaftarKasir()) {
                kasirIds.add(u.getId());
                item.add(u.getNama() + " (" + u.getId() + ")");
            }
            cmbKasir.setModel(new DefaultComboBoxModel<>(item.toArray(new String[0])));
        }
        refresh();
    }

    private DefaultTableModel buatModel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    // ---------- nama untuk tampilan ----------

    private String namaPelanggan(String id) {
        Customer c = ctx.getCustomers().cariById(id);
        return (c != null) ? c.getNama() : id;
    }

    private String namaKasir(String id) {
        User u = ctx.getAuth().cariById(id);
        return (u != null) ? u.getNama() : id;
    }

    // ---------- tampilan ----------

    private void refresh() {
        List<Transaction> tx;
        List<KoreksiLoyalitas> kor;

        if (modeKasir) {
            tx = ctx.getLaporan().riwayatPelanggan(customer.getId());
            kor = ctx.getLaporan().filterKoreksi(null, null, null);
            lblHitunganSaatIni.setText("Hitungan saat ini: "
                    + ctx.getLoyalty().getHitungan(customer.getId()) + "/" + LoyaltyTracker.TARGET);
        } else {
            try {
                int i = cmbKasir.getSelectedIndex();
                String kasirId = (i <= 0) ? null : kasirIds.get(i - 1);
                LocalDate dari = parseTanggal(txtDari.getText());
                LocalDate sampai = parseTanggal(txtSampai.getText());
                if (dari != null && sampai != null && dari.isAfter(sampai)) {
                    JOptionPane.showMessageDialog(this, "Tanggal awal tidak boleh setelah tanggal akhir",
                            "Filter", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                tx = ctx.getLaporan().filterTransaksi(kasirId, dari, sampai);
                kor = ctx.getLaporan().filterKoreksi(kasirId, dari, sampai);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Format tanggal harus yyyy-MM-dd, contoh 2026-11-20",
                        "Filter", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        tampilTx = tx;
        modelTx.setRowCount(0);
        for (Transaction t : tx) {
            modelTx.addRow(new Object[]{t.getId(), t.getTanggal().toString(),
                namaPelanggan(t.getCustomerId()), namaKasir(t.getKasirId()),
                Rupiah.format(t.getTotal()), t.getJumlahReward()});
        }

        modelKoreksi.setRowCount(0);
        for (KoreksiLoyalitas k : kor) {
            modelKoreksi.addRow(new Object[]{k.getTanggal().toString(),
                namaPelanggan(k.getCustomerId()), namaKasir(k.getKasirId()),
                k.getNilaiLama(), k.getNilaiBaru(), k.getAlasan()});
        }
    }

    /** Kosong = tanpa batas. */
    private LocalDate parseTanggal(String s) {
        String t = (s == null) ? "" : s.trim();
        return t.isEmpty() ? null : LocalDate.parse(t);
    }



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblJudul = new javax.swing.JLabel();
        pnlFilter = new javax.swing.JPanel();
        cmbKasir = new javax.swing.JComboBox<>();
        txtDari = new javax.swing.JTextField();
        txtSampai = new javax.swing.JTextField();
        btnFilter = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblTransaksi = new javax.swing.JTable();
        btnStruk = new javax.swing.JButton();
        lblHitunganSaatIni = new javax.swing.JLabel();
        txtHitunganBaru = new javax.swing.JTextField();
        txtAlasan = new javax.swing.JTextField();
        btnKoreksi = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblKoreksi = new javax.swing.JTable();
        pnlKoreksi = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblJudul.setText("jLabel1");

        javax.swing.GroupLayout pnlFilterLayout = new javax.swing.GroupLayout(pnlFilter);
        pnlFilter.setLayout(pnlFilterLayout);
        pnlFilterLayout.setHorizontalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        pnlFilterLayout.setVerticalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        cmbKasir.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        txtDari.setText("jTextField1");

        txtSampai.setText("jTextField2");

        btnFilter.setText("filter");
        btnFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterActionPerformed(evt);
            }
        });

        tblTransaksi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblTransaksi);

        btnStruk.setText("struk");
        btnStruk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStrukActionPerformed(evt);
            }
        });

        lblHitunganSaatIni.setText("jLabel1");

        txtHitunganBaru.setText("jTextField1");

        txtAlasan.setText("jTextField2");

        btnKoreksi.setText("Koreksi");
        btnKoreksi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKoreksiActionPerformed(evt);
            }
        });

        tblKoreksi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tblKoreksi);

        javax.swing.GroupLayout pnlKoreksiLayout = new javax.swing.GroupLayout(pnlKoreksi);
        pnlKoreksi.setLayout(pnlKoreksiLayout);
        pnlKoreksiLayout.setHorizontalGroup(
            pnlKoreksiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        pnlKoreksiLayout.setVerticalGroup(
            pnlKoreksiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblJudul, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbKasir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 6, Short.MAX_VALUE)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pnlKoreksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnKoreksi)
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtAlasan, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHitunganBaru, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtSampai, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnFilter, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(txtDari, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnStruk)
                    .addComponent(lblHitunganSaatIni, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(83, 83, 83))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblJudul, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtSampai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnFilter)
                                .addGap(18, 18, 18)
                                .addComponent(btnStruk)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbKasir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblHitunganSaatIni))
                        .addGap(18, 18, 18)
                        .addComponent(txtHitunganBaru, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtAlasan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(100, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(btnKoreksi)
                        .addGap(33, 33, 33)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlKoreksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterActionPerformed
        refresh();
    }//GEN-LAST:event_btnFilterActionPerformed

    private void btnStrukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStrukActionPerformed
        int row = tblTransaksi.getSelectedRow();
        if (row < 0 || row >= tampilTx.size()) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi di tabel terlebih dahulu",
                    "Belum memilih", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Transaction tx = tampilTx.get(row);
        String teks = StrukBuilder.buatUlang(tx,
                namaPelanggan(tx.getCustomerId()), namaKasir(tx.getKasirId()));

        JTextArea area = new JTextArea(teks);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new java.awt.Dimension(460, 360));
        JOptionPane.showMessageDialog(this, sp, "Struk " + tx.getId(), JOptionPane.PLAIN_MESSAGE);
   
    }//GEN-LAST:event_btnStrukActionPerformed

    private void btnKoreksiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKoreksiActionPerformed
        try {
            int baru = Validator.validasiHitungan(txtHitunganBaru.getText());
            int lama = ctx.getLoyalty().getHitungan(customer.getId());

            int ok = JOptionPane.showConfirmDialog(this,
                    "Ubah hitungan " + customer.getNama() + " dari " + lama + " menjadi " + baru + "?",
                    "Konfirmasi koreksi", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;

            ctx.getLoyalty().koreksi(Session.getUser(), customer.getId(), baru, txtAlasan.getText());
            txtHitunganBaru.setText("");
            txtAlasan.setText("");
            JOptionPane.showMessageDialog(this, "Koreksi tersimpan",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (InputTidakValidException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Input tidak valid", JOptionPane.WARNING_MESSAGE);
        } catch (AksesDitolakException | PenyimpananGagalException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Koreksi gagal", JOptionPane.ERROR_MESSAGE);
        }
        refresh();
    }//GEN-LAST:event_btnKoreksiActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFilter;
    private javax.swing.JButton btnKoreksi;
    private javax.swing.JButton btnStruk;
    private javax.swing.JComboBox<String> cmbKasir;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblHitunganSaatIni;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JPanel pnlKoreksi;
    private javax.swing.JTable tblKoreksi;
    private javax.swing.JTable tblTransaksi;
    private javax.swing.JTextField txtAlasan;
    private javax.swing.JTextField txtDari;
    private javax.swing.JTextField txtHitunganBaru;
    private javax.swing.JTextField txtSampai;
    // End of variables declaration//GEN-END:variables
}
