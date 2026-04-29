package model ;
/**
 * Table de liaison entre un enseignant, une matière et une classe.
 * Représente "qui enseigne quoi dans quelle classe".
 */
public class EnseignantMatiere {
    private int id;
    private int utilisateurId;
    private Professeur professeur;
    private int matiereId;
    private Matiere matiere;
    private int classeId;
    private Classe classe;

    public EnseignantMatiere() {}

    public EnseignantMatiere(int id, int utilisateurId, int matiereId, int classeId) {
        this.id = id; this.utilisateurId = utilisateurId;
        this.matiereId = matiereId; this.classeId = classeId;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getUtilisateurId() {
        return utilisateurId;
    }
    public void setUtilisateurId(int u) {
        this.utilisateurId = u;
    }

    public Professeur getProfesseur() {
        return professeur;
    }
    public void setProfesseur(Professeur p) {
        this.professeur = p;
    }
    public int getMatiereId() { return matiereId; }
    public void setMatiereId(int m) {
        this.matiereId = m;
    }
    public Matiere getMatiere() {
        return matiere;
    }
    public void setMatiere(Matiere m) {
        this.matiere = m;
    }

    public int getClasseId() {
        return classeId;
    }
    public void setClasseId(int c) {
        this.classeId = c;
    }

    public Classe getClasse() {
        return classe;
    }
    public void setClasse(Classe c) {
        this.classe = c;
    }

    @Override
    public String toString() { return "EnseignantMatiere{profId=" + utilisateurId + ", matiereId=" + matiereId + ", classeId=" + classeId + "}"; }
}