
import Services.Auth;
import Services.EtudiantService;
import Services.Bulletin;
import models.Etudiant;
import java.util.List;
import java.util.Collection;
import models.Utilisateur;


public class App {
    private final Auth auth;
    private final EtudiantService es;
    private final Bulletin bs;

    public App() {
        this.es = new EtudiantService();
        this.auth = new Auth();
        this.bs = new Bulletin(es);
    }

    public static void main(String[] args) {
        new App().demarrer();
    }

    void demarrer() {
       
        while (true) {
            if (!auth.estConnecte()) {
                if (!menuConnexion()) break;
            } else {
                menuRole();
            }
        }
        System.out.println("\n*** Fin du programme. Au revoir ! ***");
    }

    private boolean menuConnexion() {
        Console.titre("CONNEXION AU SYSTEME");
        System.out.println("Utilisateurs enregistres :");
        System.out.println("- admin / admin123 (Administrateur)");
        System.out.println("- prof.math / math2024 (Enseignant)");
        System.out.println("- prof.fr / fr2024 (Enseignant)"); 
        System.out.println("- agbedigni.kofi / etud001 (Etudiant)");
        System.out.println("\n[Tapez 'quitter' pour fermer le programme]\n");

        String id = Console.lire("Identifiant");
        if (id.equalsIgnoreCase("quitter")) return false;
        
        String mdp = Console.lireOpt("Mot de passe");
        
        if (auth.connecter(id, mdp)) {
            Console.ok("Bienvenue, " + auth.getUtilisateurConnecte().getNomComplet());
        } else {
            Console.err("Identifiant ou mot de passe incorrect.");
        }
        Console.pause();
        return true;
    }

    private void menuRole() {
        switch (auth.getUtilisateurConnecte().getRole()) {
            case ADMIN:      menuAdmin(); break;
            case ENSEIGNANT: menuEnseignant(); break;
            case ETUDIANT:   menuEtudiant(); break;
        }
    }

    private void menuAdmin() {
        while (auth.estConnecte()) {
            Console.titre("MENU ADMINISTRATEUR");
            System.out.println("1. Gestion des etudiants");
            System.out.println("2. Gestion des notes");
            System.out.println("3. Bulletins scolaires");
            System.out.println("4. Gestion des utilisateurs");
            System.out.println("5. Statistiques");
            System.out.println("0. Se deconnecter\n");

            switch (Console.lireInt("Choix", 0, 5)) {
                case 1: menuEtudiants(); 
                    break;
                case 2: menuNotes(null); 
                    break;
                case 3: menuBulletins(); 
                    break;
                case 4: menuUtilisateurs(); 
                    break;
                case 5: menuStats(); 
                    break;
                case 0: auth.deconnecter(); 
                    return;
            }
        }
    }

    private void menuEnseignant() {
        while (auth.estConnecte()) {
            Console.titre("MENU ENSEIGNANT");
            System.out.println("1. Consulter la liste des etudiants");
            System.out.println("2. Saisie des notes");
            System.out.println("3. Consulter un bulletin");
            System.out.println("0. Se deconnecter\n");

            switch (Console.lireInt("Choix", 0, 3)) {
                case 1: listerTous(); Console.pause(); break;
                case 2: menuNotes(null); break;
                case 3: bs.afficherBulletin(Console.lire("Matricule")); Console.pause(); break;
                case 0: auth.deconnecter(); return;
            }
        }
    }

    private void menuEtudiant() {
        String mat = auth.getUtilisateurConnecte().getMatriculeEtudiant();
        while (auth.estConnecte()) {
            Console.titre("ESPACE ETUDIANT");
            System.out.println("1. Voir mes notes");
            System.out.println("2. Voir mon bulletin");
            System.out.println("0. Se deconnecter\n");

            switch (Console.lireInt("Choix", 0, 2)) {
                case 1: afficherNotes(mat); Console.pause(); break;
                case 2: bs.afficherBulletin(mat); Console.pause(); break;
                case 0: auth.deconnecter(); return;
            }
        }
    }

    private void menuEtudiants() {
        while (true) {
            Console.sousTitre("Gestion des Etudiants");
            System.out.println("1. Ajouter  ");
            System.out.println("2. Modifier  ");
            System.out.println("3. Supprimer");
            System.out.println("4. Lister tout ");
            System.out.println("5. Rechercher ");
            System.out.println("6. Par classe");
            System.out.println("0. Retour\n");

            int c = Console.lireInt("Choix", 0, 6);
            switch (c) {
                case 1: ajouterEtudiant(); 
                    break;
                case 2: modifierEtudiant();
                     break;
                case 3: supprimerEtudiant();
                     break;
                case 4: listerTous(); 
                    break;
                case 5: rechercherEtudiant(); 
                    break;
                case 6: listerParClasse(); 
                    break;
                case 0: return;
            }
            if (c != 0) Console.pause();
        }
    }

    private void ajouterEtudiant() {
        String mat = Console.lire("Matricule");
        if (es.trouverParMatricule(mat) != null) {
            Console.err("Etudiant deja existant!");
            return;
        }
        String nom = Console.lire("Nom");
        String prenom = Console.lire("Prenom");
        String date = Console.lireOpt("Date naissance (jj/mm/aaaa)");
        String classe = Console.lire("Classe");

        if (es.ajouterEtudiant(mat, nom, prenom, date, classe)) {
            Console.ok("Etudiant ajoute!");
        } else {
            Console.err("Erreur lors de l'ajout");
        }
    }

    private void modifierEtudiant() {
        String mat = Console.lire("Matricule a modifier");
        Etudiant e = es.trouverParMatricule(mat);
        if (e == null) {
            Console.err("Etudiant introuvable!");
            return;
        }
        Console.info(e.toString());
        if (Console.confirmer("Modifier?")) {
            String nom = Console.lire("Nouveau nom");
            String prenom = Console.lire("Nouveau prenom");
            String date = Console.lireOpt("Nouvelle date naissance");
            String classe = Console.lire("Nouvelle classe");
            if (es.modifierEtudiant(mat, nom, prenom, date, classe)) {
                Console.ok("Etudiant modifie!");
            }
        }
    }

    private void supprimerEtudiant() {
        String mat = Console.lire("Matricule a supprimer");
        if (Console.confirmer("Supprimer definitivement?")) {
            if (es.supprimerEtudiant(mat)) {
                Console.ok("Etudiant supprime!");
            } else {
                Console.err("Etudiant introuvable!");
            }
        }
    }

    private void listerTous() {
        Collection<Etudiant> tous = es.getTousEtudiants();
        Console.sousTitre("Liste des Etudiants (" + tous.size() + ")");
        System.out.printf("%-10s %-15s %-12s %-8s %6s%n", "MATRICULE", "NOM", "PRENOM", "CLASSE", "MOY.");
        System.out.println("-".repeat(55));
        for (Etudiant e : tous) {
            System.out.printf("%-10s %-15s %-12s %-8s %6.2f%n",
                e.getMatricule(), e.getNom(), e.getPrenom(), e.getClasse(), e.getMoyenneGenerale());
        }
    }

    private void rechercherEtudiant() {
        String mot = Console.lireOpt("Mot-cle de recherche");
        List<Etudiant> resultats = es.rechercher(mot);
        Console.sousTitre("Resultats (" + resultats.size() + ")");
        for (Etudiant e : resultats) {
            System.out.println("  " + e.toString());
        }
    }

    private void listerParClasse() {
        List<String> classes = es.getClasses();
        Console.sousTitre("Classes disponibles");
        for (int i = 0; i < classes.size(); i++) {
            System.out.println((i+1) + ". " + classes.get(i));
        }
        if (classes.isEmpty()) return;
        
        int choix = Console.lireInt("Classe", 1, classes.size()) - 1;
        String classe = classes.get(choix);
        List<Etudiant> liste = es.getEtudiantsParClasse(classe);
        Console.sousTitre("Classe " + classe + " (" + liste.size() + ")");
        for (Etudiant e : liste) {
            System.out.printf("  %s%n", e.toString());
        }
    }

    private void menuNotes(String matFixe) {
        while (true) {
            Console.sousTitre("Gestion des Notes");
            System.out.println("1. Saisir une note");
            System.out.println("2. Consulter les notes d'un etudiant");
            System.out.println("0. Retour\n");
            
            int c = Console.lireInt("Choix", 0, 2);
            switch (c) {
                case 1: saisirNote(matFixe); break;
                case 2: afficherNotes(matFixe != null ? matFixe : Console.lire("Matricule")); break;
                case 0: return;
            }
            if (c != 0) Console.pause();
        }
    }

    private void saisirNote(String matFixe) {
        String mat = matFixe != null ? matFixe : Console.lire("Matricule");
        Etudiant e = es.trouverParMatricule(mat);
        if (e == null) {
            Console.err("Etudiant introuvable!");
            return;
        }
        Console.info(e.toString());
        String matiere = Console.lire("Matiere");
        String trimestre = Console.lireOpt("Trimestre (Trimestre 1/2/3)");
        double note = Console.lireNote("Note");

        if (es.saisirNote(mat, matiere, trimestre, note)) {
            Console.ok("Note saisie avec succes!");
        } else {
            Console.err("Note invalide ou trimestre incorrect!");
        }
    }

    private void afficherNotes(String mat) {
        Etudiant e = es.trouverParMatricule(mat);
        if (e == null) {
            Console.err("Etudiant introuvable!");
            return;
        }
        Console.sousTitre("Notes de " + e.getNom() + " " + e.getPrenom());
        var notes = e.getNotes();
        if (notes.isEmpty()) {
            Console.info("Aucune note");
            return;
        }
        for (var matiere : notes.entrySet()) {
            System.out.println("  " + matiere.getKey() + ":");
            for (var trim : matiere.getValue().entrySet()) {
                System.out.printf("    %s: %.2f%n", trim.getKey(), trim.getValue());
            }
            System.out.printf("    Moyenne matiere: %.2f%n", e.getMoyenneMatiere(matiere.getKey()));
        }
        System.out.printf("  MOYENNE GENERALE: %.2f (%s)%n", e.getMoyenneGenerale(), e.getAppreciation());
    }

    private void menuBulletins() {
        Console.sousTitre("Bulletins Scolaires");
        String mat = Console.lire("Matricule");
        bs.afficherBulletin(mat);
        Console.pause();
    }

    private void menuUtilisateurs() {
        while (true) {
            Console.sousTitre("Gestion Utilisateurs");
            System.out.println("1. Lister ");
            System.out.println("2. Ajouter ");
            System.out.println(" 3. Supprimer");
            System.out.println("0. Retour");
            int c = Console.lireInt("Choix", 0, 3);
            if (c == 0) break;
            if (c == 1) listerUtilisateurs();
            if (c == 2) ajouterUtilisateur();
            if (c == 3) supprimerUtilisateur();
            Console.pause();
        }
    }

    private void listerUtilisateurs() {
        var tous = auth.getTousUtilisateurs();
        Console.sousTitre("Utilisateurs (" + tous.size() + ")");
        for (Utilisateur u : tous.values()) {
            System.out.printf("  %s (%s) - %s%n", 
                u.getIdentifiant(), u.getRole(), u.getNomComplet());
        }
    }

    private void ajouterUtilisateur() {
        String id = Console.lire("Identifiant");
        String mdp = Console.lire("Mot de passe");
        String nom = Console.lire("Nom complet");
        Console.sousTitre("Role: 1=Admin, 2=Enseignant, 3=Etudiant");
        int r = Console.lireInt("Role", 1, 3);
        Utilisateur.Role role = switch(r) {
            case 1 -> Utilisateur.Role.ADMIN;
            case 2 -> Utilisateur.Role.ENSEIGNANT;
            default -> Utilisateur.Role.ETUDIANT;
        };
        if (auth.ajouterUtilisateur(id, mdp, role, nom)) {
            Console.ok("Utilisateur cree!");
        } else {
            Console.err("Identifiant existe deja!");
        }
    }

    private void supprimerUtilisateur() {
        String id = Console.lire("Identifiant a supprimer");
        if (Console.confirmer("Supprimer?")) {
            if (auth.supprimerUtilisateur(id)) {
                Console.ok("Supprime!");
            } else {
                Console.err("Introuvable!");
            }
        }
    }

    private void menuStats() {
        Console.sousTitre("Statistiques de Performance");
        System.out.printf("%-12s %-6s %-8s %-8s %-8s%n", "CLASSE", "EFFECT.", "MIN", "MAX", "MOY.");
        System.out.println("-".repeat(50));
        for (String classe : es.getClasses()) {
            List<Etudiant> liste = es.getEtudiantsParClasse(classe);
            double min = liste.stream().mapToDouble(Etudiant::getMoyenneGenerale).min().orElse(0);
            double max = liste.stream().mapToDouble(Etudiant::getMoyenneGenerale).max().orElse(0);
            double moy = liste.stream().mapToDouble(Etudiant::getMoyenneGenerale).average().orElse(0);
            System.out.printf("%-12s %-6d %-8.2f %-8.2f %-8.2f%n", 
                classe, liste.size(), min, max, moy);
        }
        Console.pause();
    }

   
}