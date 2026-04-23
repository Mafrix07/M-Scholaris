package models;

import java.util.LinkedHashMap;
import java.util.Map;

public class Etudiant {
    private String matricule;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private String classe;
    private Map<String, Map<String, Double>> notes;

    public Etudiant(String matricule, String nom, String prenom, String dateNaissance, String classe) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.classe = classe;
        this.notes = new LinkedHashMap<>();
    }

    
    public String getMatricule() {
         return matricule; 
        }
    public String getNom() { 
        return nom; 
    }
    public void setNom(String nom) { 
        this.nom = nom; 
    }
    public String getPrenom() { 
        return prenom; 
    }
    public void setPrenom(String prenom) { 
        this.prenom = prenom; 
    }
    public String getDateNaissance() { 
        return dateNaissance; 
    }
    public void setDateNaissance(String dateNaissance) { 
        this.dateNaissance = dateNaissance; 
    }
    public String getClasse() { 
        return classe; 
    }
    public void setClasse(String classe) { 
        this.classe = classe; 
    }
    public Map<String, Map<String, Double>> getNotes() { 
        return notes; }

    public void ajouterNote(String matiere, String trimestre, double note) {
        notes.computeIfAbsent(matiere, k -> new LinkedHashMap<>()).put(trimestre, note);
    }

    public double getMoyenneMatiere(String matiere) {
        Map<String, Double> trimestres = notes.get(matiere);
        if (trimestres == null || trimestres.isEmpty()) return 0.0;
        double somme = 0;
        for (double note : trimestres.values()) somme += note;
        return somme / trimestres.size();
    }

    public double getMoyenneGenerale() {
        if (notes.isEmpty()) return 0.0;
        double somme = 0;
        for (String matiere : notes.keySet()) somme += getMoyenneMatiere(matiere);
        return somme / notes.size();
    }

    public String getAppreciation() {
        double moy = getMoyenneGenerale();
        if (moy >= 16) return "Très Bien";
        else if (moy >= 14) return "Bien";
        else if (moy >= 12) return "Assez Bien";
        else if (moy >= 10) return "Passable";
        else return "Insuffisant";
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s) - %s - Moy: %.2f", 
            nom.toUpperCase(), prenom, matricule, classe, getMoyenneGenerale());
    }
}