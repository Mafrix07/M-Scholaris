package Services;

import models.Etudiant;
import java.util.Map;

public class Bulletin {
    private static final String ETABLISSEMENT = "Lycee iP Net Institute of Technology";
    private static final String ANNEE_SCOLAIRE = "2025-2026";
    private static final int LARGEUR = 70;

    private EtudiantService etudiantService;

    public Bulletin(EtudiantService etudiantService) {
        this.etudiantService = etudiantService;
    }

    public void afficherBulletin(String matricule) {
        Etudiant et = etudiantService.trouverParMatricule(matricule);
        if (et == null) {
            System.out.println("  [!] Etudiant introuvable (Matricule: " + matricule + ")");
            return;
        }

        int rang = etudiantService.getRang(matricule);
        int totalClasse = etudiantService.getEtudiantsParClasse(et.getClasse()).size();

        // En-tete
        ligne('=');
        centrer("BULLETIN SCOLAIRE", '=');
        ligne('=');
        System.out.println();

        System.out.printf("  Etablissement : ", ETABLISSEMENT);
        System.out.printf("  Annee scolaire : ", ANNEE_SCOLAIRE);
        System.out.println();

        ligne('-');

        // Informations eleve
        System.out.printf("  Matricule  :   Classe : ", et.getMatricule(), et.getClasse());
        System.out.printf("  Nom        :   Prenom :n", et.getNom().toUpperCase(), et.getPrenom());
        System.out.printf("  Date naiss : ", et.getDateNaissance());
        System.out.println();

        ligne('-');

        // Tableau des notes
        System.out.printf( "MATIERE", "TRIMESTRE", "NOTE");
        ligne('-');

        Map<String, Map<String, Double>> notes = et.getNotes();
        if (notes == null || notes.isEmpty()) {
            System.out.println("   --- Aucune note enregistree pour le moment ---");
        } else {
            for (Map.Entry<String, Map<String, Double>> matEntry : notes.entrySet()) {
                String matiere = matEntry.getKey();
                Map<String, Double> trimestres = matEntry.getValue();

                for (Map.Entry<String, Double> trimEntry : trimestres.entrySet()) {
                    System.out.printf("  %-25s %-15s %5.2f / 20%n",
                            matiere, trimEntry.getKey(), trimEntry.getValue());
                }
                
                // Affichage de la moyenne par matiere
                System.out.printf("  %-25s %-15s %5.2f / 20  (Moyenne Matiere)%n",
                        "", ">> MOYENNE", et.getMoyenneMatiere(matiere));
                ligne('.');
            }
        }

        ligne('-');

        
        double moyenneG = et.getMoyenneGenerale();
        System.out.printf("  MOYENNE GENERALE  : %5.2f / 20%n", moyenneG);
        System.out.printf("  APPRECIATION      : %s%n", et.getAppreciation());
        System.out.printf("  RANG DANS CLASSE  : %d / %d%n", rang, totalClasse);
        System.out.println();

        ligne('=');
        System.out.println("  [Genere par SGS - iP Net Institute]");
        ligne('=');
        System.out.println();
    }

    private void ligne(char c) {
        System.out.println("  " + String.valueOf(c).repeat(LARGEUR));
    }

    private void centrer(String texte, char bordure) {
        int longueurTotale = LARGEUR;
        int espaces = (longueurTotale - texte.length() - 2) / 2;
        if (espaces < 0) espaces = 0;
        
        String cote = String.valueOf(bordure).repeat(espaces);
        System.out.printf( cote, texte, cote);
    }
}