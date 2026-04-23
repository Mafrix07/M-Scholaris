package Services;

import models.Etudiant;
import java.util.*;
import java.util.stream.Collectors;

public class EtudiantService {
    private static final Set<String> TRIMESTRES_VALIDES = Set.of(
        "Trimestre 1", "Trimestre 2", "Trimestre 3"
    );

    private Map<String, Etudiant> etudiants;

    public EtudiantService() {
        this.etudiants = new LinkedHashMap<>();
        chargerDonneesDemo();
    }

    public boolean ajouterEtudiant(String matricule, String nom, String prenom,
                                  String dateNaissance, String classe) {
        if (etudiants.containsKey(matricule)) return false;
        etudiants.put(matricule, new Etudiant(matricule, nom, prenom, dateNaissance, classe));
        return true;
    }

    public boolean modifierEtudiant(String matricule, String nom, String prenom,
                                   String dateNaissance, String classe) {
        Etudiant e = etudiants.get(matricule);
        if (e == null) return false;
        e.setNom(nom);
        e.setPrenom(prenom);
        e.setDateNaissance(dateNaissance);
        e.setClasse(classe);
        return true;
    }

    public boolean supprimerEtudiant(String matricule) {
        return etudiants.remove(matricule) != null;
    }

    public Etudiant trouverParMatricule(String matricule) {
        return etudiants.get(matricule);
    }

    public List<Etudiant> rechercher(String motCle) {
        if (motCle == null || motCle.isBlank()) return new ArrayList<>(etudiants.values());
        String mc = motCle.toLowerCase();
        return etudiants.values().stream()
                .filter(e -> (e.getNom() != null && e.getNom().toLowerCase().contains(mc))
                         || (e.getPrenom() != null && e.getPrenom().toLowerCase().contains(mc))
                         || (e.getMatricule() != null && e.getMatricule().toLowerCase().contains(mc)))
                .collect(Collectors.toList());
    }

    public List<Etudiant> getEtudiantsParClasse(String classe) {
        return etudiants.values().stream()
                .filter(e -> e.getClasse().equalsIgnoreCase(classe))
                .sorted(Comparator.comparing(Etudiant::getNom))
                .collect(Collectors.toList());
    }

    public Collection<Etudiant> getTousEtudiants() {
        return etudiants.values();
    }

    public boolean saisirNote(String matricule, String matiere, String trimestre, double note) {
        if (note < 0 || note > 20) return false;
        if (!TRIMESTRES_VALIDES.contains(trimestre)) return false;
        Etudiant e = etudiants.get(matricule);
        if (e == null) return false;
        e.ajouterNote(matiere, trimestre, note);
        return true;
    }

    public int getRang(String matricule) {
        Etudiant cible = etudiants.get(matricule);
        if (cible == null) return -1;

        List<Etudiant> classemates = etudiants.values().stream()
                .filter(e -> e.getClasse().equalsIgnoreCase(cible.getClasse()))
                .sorted(Comparator.comparingDouble(Etudiant::getMoyenneGenerale).reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < classemates.size(); i++) {
            if (classemates.get(i).getMatricule().equals(matricule)) return i + 1;
        }
        return -1;
    }

    public List<String> getClasses() {
        return etudiants.values().stream()
                .map(Etudiant::getClasse)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private void chargerDonneesDemo() {
        ajouterEtudiant("2024001", "AGBEDIGNI", "Kofi", "12/03/2009", "3ème A");
        ajouterEtudiant("2024002", "D'ALMEIDA", "Akosua", "05/07/2009", "3ème A");
        ajouterEtudiant("2024003", "AMAVI", "Mensah", "20/01/2009", "3ème A");
        ajouterEtudiant("2024004", "KOFFI", "Ama", "15/09/2008", "2nde B");
        ajouterEtudiant("2024005", "DOSSOU", "Yao", "30/11/2008", "2nde B");

        saisirNote("2024001", "Mathématiques", "Trimestre 1", 15.5);
        saisirNote("2024001", "Français", "Trimestre 1", 14.0);
        saisirNote("2024001", "Physique-Chimie", "Trimestre 1", 12.5);
        saisirNote("2024001", "Mathématiques", "Trimestre 2", 16.0);

        saisirNote("2024002", "Mathématiques", "Trimestre 1", 18.0);
        saisirNote("2024002", "Français", "Trimestre 1", 16.5);
        saisirNote("2024002", "Physique-Chimie", "Trimestre 1", 17.0);

        saisirNote("2024003", "Mathématiques", "Trimestre 1", 9.5);
        saisirNote("2024003", "Français", "Trimestre 1", 11.0);
        saisirNote("2024003", "Physique-Chimie", "Trimestre 1", 8.0);

        saisirNote("2024004", "Mathématiques", "Trimestre 1", 13.0);
        saisirNote("2024004", "Français", "Trimestre 1", 15.0);

        saisirNote("2024005", "Mathématiques", "Trimestre 1", 10.5);
        saisirNote("2024005", "Français", "Trimestre 1", 12.0);
    }
}