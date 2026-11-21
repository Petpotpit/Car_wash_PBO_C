package carwash.logic;

import carwash.data.CsvRepository;

import java.io.IOException;

public class AppContext {

    private static AppContext instance;

    private final CsvRepository repo = new CsvRepository();
    private final AuthService auth;
    private final HolidayCalendar kalender;
    private final CustomerService customers;
    private final CatalogService catalog;
    private final LoyaltyTracker loyalty;
    private final CheckoutService checkout;
    private final LaporanService laporan;

    private AppContext() throws IOException {
        auth = new AuthService(repo.muatUsers());
        kalender = new HolidayCalendar(repo.muatTanggalMerah());
        customers = new CustomerService(repo, repo.muatCustomers());
        catalog = new CatalogService(repo, repo.muatKatalog());
        loyalty = new LoyaltyTracker(repo, kalender, repo.muatLoyalty(), repo.muatKoreksi());
        checkout = new CheckoutService(catalog, loyalty, repo, repo.muatTransaksi());
        laporan = new LaporanService(checkout, loyalty);
    }

    /** Dipanggil sekali di Main, sebelum form apa pun dibuka. */
    public static void muat() throws IOException {
        instance = new AppContext();
    }

    public static AppContext get() {
        if (instance == null) {
            throw new IllegalStateException("AppContext belum dimuat. Panggil AppContext.muat() di Main.");
        }
        return instance;
    }

    public CsvRepository getRepo() { return repo; }
    public AuthService getAuth() { return auth; }
    public HolidayCalendar getKalender() { return kalender; }
    public CustomerService getCustomers() { return customers; }
    public CatalogService getCatalog() { return catalog; }
    public LoyaltyTracker getLoyalty() { return loyalty; }
    public CheckoutService getCheckout() { return checkout; }
    public LaporanService getLaporan() { return laporan; }
}