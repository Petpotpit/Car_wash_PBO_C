/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package carwash.ui;

import carwash.exception.AksesDitolakException;
import carwash.exception.InputTidakValidException;
import carwash.exception.PenyimpananGagalException;
import carwash.exception.StokTidakCukupException;
import carwash.exception.TotalBerubahException;
import carwash.logic.AppContext;
import carwash.logic.Cart;
import carwash.logic.LoyaltyResult;
import carwash.logic.LoyaltyTracker;
import carwash.logic.Rupiah;
import carwash.logic.Session;
import carwash.logic.StrukBuilder;
import carwash.logic.Validator;
import carwash.model.CartItem;
import carwash.model.Customer;
import carwash.model.Item;
import carwash.model.Product;
import carwash.model.Role;
import carwash.model.Transaction;
import carwash.model.TransactionLine;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author argor
 */
public class FormTransaksi extends javax.swing.JFrame {

    private final AppContext ctx = AppContext.get();
    private final Cart cart = new Cart();
    private final Customer customer;
    private DefaultTableModel modelKatalog;
    private DefaultTableModel modelKeranjang;

    public FormTransaksi(Customer customer) {
        this.customer = customer;
        initComponents();

        // Lapis kedua: hanya Kasir
        if (!Session.isLoggedIn() || Session.getUser().getRole() != Role.KASIR) {
            dispose();
            new FormLogin().setVisible(true);
            return;
        }

        setLocationRelativeTo(null);
        lblPelanggan.setText("Pelanggan: " + customer.getNama() + " (" + customer.getId() + ")");
        txtQty.setText("1");
        txtPreview.setEditable(false);
        txtPreview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        lblBadge.setForeground(new Color(0, 128, 0));
        lblBadge.setFont(lblBadge.getFont().deriveFont(Font.BOLD));

        modelKatalog = buatModel("ID", "Nama", "Kategori", "Harga", "Stok");
        tblKatalog.setModel(modelKatalog);
        tblKatalog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        modelKeranjang = buatModel("ID", "Nama", "Qty", "Subtotal");
        tblKeranjang.setModel(modelKeranjang);
        tblKeranjang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // stok dan tanggal bisa berubah saat jendela lain dipakai
        addWindowListener(new WindowAdapter() {
            @Override public void windowActivated(WindowEvent e) {
                refreshKatalog();
                refreshPreview();
            }
        });

        refreshKatalog();
        refreshKeranjang();
    }

    private DefaultTableModel buatModel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    // ---------- tampilan ----------

    private void refreshKatalog() {
        modelKatalog.setRowCount(0);
        for (Item it : ctx.getCatalog().getDaftar()) {
            Object stok = (it instanceof Product p) ? p.getStok() : "-";
            modelKatalog.addRow(new Object[]{it.getId(), it.getNama(), it.getKategori(),
                Rupiah.format(it.getHarga()), stok});
        }
    }

    private void refreshKeranjang() {
        modelKeranjang.setRowCount(0);
        for (CartItem ci : cart.getIsi()) {
            modelKeranjang.addRow(new Object[]{ci.getItem().getId(), ci.getItem().getNama(),
                ci.getQty(), Rupiah.format(ci.getItem().hitungHarga(ci.getQty()))});
        }
        refreshPreview();
    }

    /** Dipanggil setiap keranjang berubah. Selalu memakai tanggal hari ini. */
    private void refreshPreview() {
        LoyaltyResult r = ctx.getCheckout().previewCheckout(customer, cart);

        if (cart.isKosong()) {
            txtPreview.setText("Keranjang kosong");
        } else {
            StringBuilder sb = new StringBuilder();
            for (TransactionLine l : r.getBarisTampil()) {
                sb.append(String.format("%-34s %12s%n",
                        l.getNamaItem() + " x" + l.getQty()
                                + (l.isReward() ? " (GRATIS - reward)" : ""),
                        Rupiah.format(l.getSubtotal())));
            }
            sb.append(String.format("%nSubtotal : %s%nPotongan : %s%nTotal    : %s%n",
                    Rupiah.format(r.getTotal() + r.getPotongan()),
                    Rupiah.format(r.getPotongan()),
                    Rupiah.format(r.getTotal())));
            txtPreview.setText(sb.toString());
        }

        // label hitungan
        int awal = r.getHitunganAwal();
        int akhir = r.getHitunganAkhir();
        if (!r.isAktifHariIni()) {
            lblHitungan.setText("Program loyalitas tidak aktif hari ini (Minggu/tanggal merah)");
        } else if (awal != akhir) {
            lblHitungan.setText("Cuci berbayar: " + awal + "/" + LoyaltyTracker.TARGET
                    + " -> " + akhir + "/" + LoyaltyTracker.TARGET + " setelah transaksi ini");
        } else {
            lblHitungan.setText("Cuci berbayar: " + awal + "/" + LoyaltyTracker.TARGET);
        }

        // badge
        if (r.isAktifHariIni() && r.getJumlahGratis() > 0) {
            lblBadge.setText("Cuci GRATIS (reward) dipakai: " + r.getJumlahGratis() + " unit");
        } else if (r.isAktifHariIni() && awal == LoyaltyTracker.TARGET) {
            lblBadge.setText("Reward siap dipakai");
        } else {
            lblBadge.setText(" ");
        }

        lblTotal.setText("Total: " + Rupiah.format(r.getTotal()));
        btnBayar.setEnabled(!cart.isKosong());
    }

    private String idDariTabel(javax.swing.JTable tabel, DefaultTableModel model) {
        int row = tabel.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris di tabel terlebih dahulu",
                    "Belum memilih", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return (String) model.getValueAt(row, 0);
    }

    private void pesan(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(),
                "Tidak dapat diproses", JOptionPane.WARNING_MESSAGE);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblPelanggan = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblKatalog = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        txtQty = new javax.swing.JTextField();
        btnTambah = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblKeranjang = new javax.swing.JTable();
        btnSetQty = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtPreview = new javax.swing.JTextArea();
        lblHitungan = new javax.swing.JLabel();
        lblBadge = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        btnBayar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblPelanggan.setText("jLabel1");

        tblKatalog.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblKatalog);

        jLabel2.setText("Qty");

        btnTambah.setText("Tambah");
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahActionPerformed(evt);
            }
        });

        tblKeranjang.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tblKeranjang);

        btnSetQty.setText("Set Qty");
        btnSetQty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetQtyActionPerformed(evt);
            }
        });

        btnHapus.setText("Hapus Item");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        txtPreview.setColumns(20);
        txtPreview.setRows(5);
        jScrollPane3.setViewportView(txtPreview);

        lblHitungan.setText("jLabel3");

        lblBadge.setText("jLabel4");

        lblTotal.setText("jLabel5");

        btnBayar.setText("Bayar");
        btnBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBayarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtQty, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41)
                        .addComponent(btnTambah))
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 84, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBayar)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSetQty)
                        .addGap(44, 44, 44)
                        .addComponent(btnHapus))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(lblTotal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                        .addComponent(lblBadge, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblHitungan, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblPelanggan)
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtQty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnTambah, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSetQty)
                        .addComponent(btnHapus)))
                .addGap(32, 32, 32)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblHitungan)
                .addGap(18, 18, 18)
                .addComponent(lblBadge)
                .addGap(18, 18, 18)
                .addComponent(lblTotal)
                .addGap(18, 18, 18)
                .addComponent(btnBayar)
                .addContainerGap(58, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahActionPerformed
       String id = idDariTabel(tblKatalog, modelKatalog);
        if (id == null) return;
        try {
            int qty = Validator.validasiQty(txtQty.getText());
            Item item = ctx.getCatalog().cariById(id);
            if (item == null) throw new InputTidakValidException("Item tidak ditemukan di katalog");
            cart.tambah(item, qty);
            txtQty.setText("1");
        } catch (InputTidakValidException | StokTidakCukupException e) {
            pesan(e);
        }
        refreshKeranjang();
    }//GEN-LAST:event_btnTambahActionPerformed

    private void btnSetQtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetQtyActionPerformed
        String id = idDariTabel(tblKeranjang, modelKeranjang);
        if (id == null) return;
        try {
            cart.ubahQty(id, Validator.validasiQty(txtQty.getText()));
        } catch (InputTidakValidException | StokTidakCukupException e) {
            pesan(e);
        }
        refreshKeranjang();
    }//GEN-LAST:event_btnSetQtyActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        String id = idDariTabel(tblKeranjang, modelKeranjang);
        if (id == null) return;
        cart.hapus(id);
        refreshKeranjang();
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBayarActionPerformed
        btnBayar.setEnabled(false);                  // cegah klik ganda
        try {
            // total yang akan ditampilkan di dialog (tanggal saat Bayar ditekan)
            LoyaltyResult r = ctx.getCheckout().previewCheckout(customer, cart);
            double total = r.getTotal();

            DialogPembayaran dlg = new DialogPembayaran(this, true);
            dlg.setTotal(total);
            dlg.setVisible(true);                    // modal: menunggu dialog ditutup
            Long uang = dlg.getUang();
            if (uang == null) return;                // dibatalkan

            Transaction tx = ctx.getCheckout().checkout(
                    Session.getUser(), customer, cart, uang, total);

            String struk = StrukBuilder.buat(tx, customer.getNama(),
                    Session.getUser().getNama(), r);
            tampilkanStruk(struk, tx.getId());

        } catch (AksesDitolakException | InputTidakValidException | StokTidakCukupException
                 | TotalBerubahException | PenyimpananGagalException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Pembayaran gagal", JOptionPane.ERROR_MESSAGE);
        } finally {
            refreshKatalog();
            refreshKeranjang();                      // juga mengatur ulang tombol Bayar
        }
    }//GEN-LAST:event_btnBayarActionPerformed
    
    private void tampilkanStruk(String teks, String idTransaksi) {
        JTextArea area = new JTextArea(teks);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new java.awt.Dimension(460, 380));

        int pilihan = JOptionPane.showOptionDialog(this, sp, "Struk " + idTransaksi,
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{"Simpan ke file", "Tutup"}, "Tutup");

        if (pilihan == 0) {
            try {
                Path dir = Paths.get("data", "struk");
                Files.createDirectories(dir);
                Path file = dir.resolve(idTransaksi + ".txt");
                Files.writeString(file, teks, StandardCharsets.UTF_8);
                JOptionPane.showMessageDialog(this, "Struk disimpan di:\n" + file.toAbsolutePath(),
                        "Tersimpan", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan struk: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBayar;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnSetQty;
    private javax.swing.JButton btnTambah;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblBadge;
    private javax.swing.JLabel lblHitungan;
    private javax.swing.JLabel lblPelanggan;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTable tblKatalog;
    private javax.swing.JTable tblKeranjang;
    private javax.swing.JTextArea txtPreview;
    private javax.swing.JTextField txtQty;
    // End of variables declaration//GEN-END:variables
}
