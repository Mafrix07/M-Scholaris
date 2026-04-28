package model;
import java.time.LocalDateTime;
import java.util.List;
public class Bulletin {
    private int id;
    private int etudiantId;
    private Etudiant etudiant;
    private int classeId;
    private String periode;
    private int anneeScolaire;
    private double moyenneGenerale;
    private int rang;
    private int effectifClasse;
    private String appreciation;
    private String fichierPdf;         // chemin vers le fichier PDF généré
    private LocalDateTime genereLe;


    public Bulletin() { this.genereLe = LocalDateTime.now(); }


    public Bulletin(int id, int etudiantId, int classeId, String periode, int anneeScolaire) {
        this.id = id; this.etudiantId = etudiantId; this.classeId = classeId;
        this.periode = periode; this.anneeScolaire = anneeScolaire;
        this.genereLe = LocalDateTime.now();
    }




    // Comportement de la classe

    public void calculerAppreciation() {
        if (moyenneGenerale >= 16)      this.appreciation = "Très Bien";
        else if (moyenneGenerale >= 14) this.appreciation = "Bien";
        else if (moyenneGenerale >= 12) this.appreciation = "Assez Bien";
        else if (moyenneGenerale >= 10) this.appreciation = "Passable";
        else                            this.appreciation = "Insuffisant";
    }

    public void calculerMoyenneGenerale(List<Note> notes, List<Matiere> matieres) {
        double sommePonderee = 0;
        double sommeCoefficients = 0;
        for (Note n : notes) {
            for (Matiere m : matieres) {
                if (m.getId() == n.getMatiereId()) {
                    sommePonderee    += n.getValeur() * m.getCoefficient();
                    sommeCoefficients += m.getCoefficient();
                    break;
                }
            }
        }
        if (sommeCoefficients > 0)
            this.moyenneGenerale = Math.round((sommePonderee / sommeCoefficients) * 100.0) / 100.0;
        calculerAppreciation();
    }


    /** Affiche le bulletin dans la console (utile pour les tests). */
    public void afficher() {
        System.out.println("════════════ BULLETIN ════════════");
        System.out.println("Période     : " + periode + " " + anneeScolaire);
        System.out.println("Moyenne gén.: " + moyenneGenerale + "/20");
        System.out.println("Rang        : " + rang + " / " + effectifClasse);
        System.out.println("Appréciation: " + appreciation);
        System.out.println("PDF         : " + (fichierPdf != null ? fichierPdf : "Non généré"));
        System.out.println("══════════════════════════════════");
    }



    public int getId() {
        return id;
    } public void setId(int id) {
        this.id = id;
    }
    public int getEtudiantId() {
        return etudiantId;
    } public void setEtudiantId(int e) {
        this.etudiantId = e;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    } public void setEtudiant(Etudiant e) {
        this.etudiant = e;
    }
    public int getClasseId() {
        return classeId;
    }
    public void setClasseId(int c) {
        this.classeId = c;
    }
    public String getPeriode() {
        return periode;
    }
    public void setPeriode(String p) {
        this.periode = p;
    }

    public int getAnneeScolaire() {
        return anneeScolaire;
    }
    public void setAnneeScolaire(int a)
    { this.anneeScolaire = a;
    }
    public double getMoyenneGenerale() {
        return moyenneGenerale;
    }
    public void setMoyenneGenerale(double m) {
        this.moyenneGenerale = m; calculerAppreciation();
    }
    public int getRang() {
        return rang;
    } public void setRang(int r) {
        this.rang = r;
    }
    public int getEffectifClasse() {
        return effectifClasse;
    } public void setEffectifClasse(int e) {
        this.effectifClasse = e;
    }
    public String getAppreciation() {
        return appreciation;
    } public void setAppreciation(String a) {
        this.appreciation = a;
    }
    public String getFichierPdf() {
        return fichierPdf;
    } public void setFichierPdf(String f) {
        this.fichierPdf = f;
    }

    public LocalDateTime getGenereLe() {
        return genereLe;
    }


    public void setGenereLe(LocalDateTime g) {
        this.genereLe = g;
    }
    @Override public String toString() { return "Bulletin{etudiant=" + etudiantId + ", periode='" + periode + "', moy=" + moyenneGenerale + ", rang=" + rang + "/" + effectifClasse + "}"; }
}

