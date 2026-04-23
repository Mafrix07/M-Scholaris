import java.util.Scanner;

public class Console {
    private static final Scanner sc = new Scanner(System.in);

    public static String lire(String prompt) {
        String v;
        do {
            System.out.print("  " + prompt + " : ");
            v = sc.nextLine().trim();
            if (v.isEmpty()) System.out.println("   Ce champ ne peut pas être vide.");
        } while (v.isEmpty());
        return v;
    }

    public static String lireOpt(String prompt) {
        System.out.print("  " + prompt + " : ");
        return sc.nextLine().trim();
    }

    public static int lireInt(String prompt, int min, int max) {
        while (true) {
            System.out.print("  " + prompt + " [" + min + "-" + max + "] : ");
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min && v <= max) return v;
                System.out.printf("  ✗ Entre %d et %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  ✗ Nombre invalide.");
            }
        }
    }

    public static double lireNote(String prompt) {
        while (true) {
            System.out.print("  " + prompt + " [0-20] : ");
            try {
                double v = Double.parseDouble(sc.nextLine().trim().replace(",", "."));
                if (v >= 0 && v <= 20) return v;
                System.out.println("  ✗ Entre 0 et 20.");
            } catch (NumberFormatException e) {
                System.out.println("  ✗ Ex: 14.5");
            }
        }
    }

    public static boolean confirmer(String prompt) {
        System.out.print("  " + prompt + " [o/n] : ");
        String r = sc.nextLine().trim().toLowerCase();
        return r.equals("o") || r.equals("oui");
    }

    public static void pause() {
        System.out.print("\n   Entrée pour continuer...");
        sc.nextLine();
    }

    public static void titre(String t) {
        System.out.println("\n  ╔══════════════════════════════════════════╗");
        System.out.printf("  ║  %-42s║%n", t);
        System.out.println("  ╚══════════════════════════════════════════╝\n");
    }

    public static void sousTitre(String t) {
        System.out.println("\n  ┌─ " + t + " " + "─".repeat(Math.max(0, 42 - t.length())) + "┐\n");
    }

    public static void ok(String m)  { System.out.println("  ✓ " + m); }
    public static void err(String m) { System.out.println("   ERREUR : " + m); }
    public static void info(String m){ System.out.println("  ℹ  " + m); }
}