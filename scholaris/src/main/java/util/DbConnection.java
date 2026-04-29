package util ;


import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Classe utilitaire de connexion à PostgreSQL.
 * Pattern Singleton — lit les credentials depuis database.properties.
 * Le fichier database.properties doit être dans src/main/resources/config/
 */
public class DbConnection {

    private static Connection instance;
    private static Properties props;

    private DbConnection() {}

    // ── Chargement du fichier properties ──────────────────
    private static Properties getProperties() {
        if (props == null) {
            props = new Properties();
            try (InputStream input = DbConnection.class
                    .getClassLoader()
                    .getResourceAsStream("config/database.properties")) {

                if (input == null) {
                    throw new RuntimeException(
                            "❌ Fichier 'config/database.properties' introuvable !\n" +
                                    "   Vérifie qu'il est dans src/main/resources/config/"
                    );
                }
                props.load(input);
                System.out.println("✅ Credentials chargés depuis database.properties");

            } catch (IOException e) {
                throw new RuntimeException(
                        "Erreur lecture database.properties : " + e.getMessage()
                );
            }
        }
        return props;
    }

    // ── Connexion Singleton ────────────────────────────────
    /**
     * Retourne l'instance unique de connexion PostgreSQL.
     * Recrée la connexion automatiquement si elle est fermée.
     */
    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {

            Properties p = getProperties();

            String url      = p.getProperty("db.url");
            String user     = p.getProperty("db.user");
            String password = p.getProperty("db.password");
            String driver   = p.getProperty("db.driver");

            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                        "Driver PostgreSQL introuvable : " + e.getMessage()
                );
            }

            instance = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connexion PostgreSQL établie → " + url);
        }
        return instance;
    }

    // ── Fermeture propre ───────────────────────────────────
    public static void closeConnection() {
        if (instance != null) {
            try {
                instance.close();
                System.out.println("🔌 Connexion PostgreSQL fermée.");
            } catch (SQLException e) {
                System.err.println("Erreur fermeture : " + e.getMessage());
            }
        }
    }
}
 