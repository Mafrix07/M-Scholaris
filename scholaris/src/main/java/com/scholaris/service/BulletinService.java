package com.scholaris.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.scholaris.dao.BulletinDAO;
import com.scholaris.dao.EtudiantDAO;
import com.scholaris.dao.RangDAO;
import com.scholaris.dao.MatiereDAO;
import com.scholaris.model.Bulletin;
import com.scholaris.model.Etudiant;
import com.scholaris.model.Rang;
import com.scholaris.model.Matiere;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Service pour la gestion des bulletins et génération PDF via iText.
 */
public class BulletinService {

    private BulletinDAO    bulletinDAO;
    private EtudiantDAO    etudiantDAO;
    private MoyenneService moyenneService;
    private RangService    rangService;
    private RangDAO        rangDAO;
    private MatiereDAO     matiereDAO;

    public BulletinService() throws SQLException {
        this.bulletinDAO    = new BulletinDAO();
        this.etudiantDAO    = new EtudiantDAO();
        this.moyenneService = new MoyenneService();
        this.rangService    = new RangService();
        this.rangDAO        = new RangDAO();
        this.matiereDAO     = new MatiereDAO();
    }

    /**
     * Génère un bulletin complet (moyenne, rang, appréciation, PDF).
     */
    public Bulletin genererEtSauvegarderBulletin(int etudiantId, String periode, int annee) throws SQLException, IOException {
        Etudiant e = etudiantDAO.trouverParId(etudiantId);
        if (e == null) return null;

        double moyGen = moyenneService.calculerMoyenneGenerale(etudiantId, periode, annee);
        Rang r = rangDAO.trouverRangGeneral(etudiantId, periode, annee);
        int rangVal = (r != null) ? r.getRang() : 0;

        Bulletin b = bulletinDAO.trouverParEtudiantEtPeriode(etudiantId, periode, annee);
        if (b == null) {
            b = new Bulletin();
            b.setEtudiant(e);
            b.setClasse(e.getClasse());
            b.setPeriode(periode);
            b.setAnneeScolaire(annee);
        }

        b.setMoyenneGenerale(moyGen);
        b.setRang(rangVal);
        b.setAppreciation(attribuerAppreciation(moyGen));
        
        // Chemin du PDF
        String fileName = "Bulletin_" + e.getNom() + "_" + periode + "_" + annee + ".pdf";
        String filePath = "target/bulletins/" + fileName;
        new File("target/bulletins/").mkdirs();
        
        genererPdf(b, r, filePath);
        b.setFichierPdf(filePath);

        if (b.getId() == 0) bulletinDAO.ajouter(b);
        else bulletinDAO.modifier(b);

        return b;
    }

    /**
     * Attribution de l'appréciation automatique selon la moyenne.
     */
    private String attribuerAppreciation(double moyenne) {
        if (moyenne >= 16) return "Très Bien";
        if (moyenne >= 14) return "Bien";
        if (moyenne >= 12) return "Assez Bien";
        if (moyenne >= 10) return "Passable";
        return "Insuffisant";
    }

    /**
     * Génération du fichier PDF avec iText.
     */
    private void genererPdf(Bulletin b, Rang r, String path) throws IOException, SQLException {
        PdfWriter writer = new PdfWriter(path);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("BULLETIN DE NOTES").setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Année Scolaire : " + b.getAnneeScolaire()));
        document.add(new Paragraph("Période : " + b.getPeriode()));
        document.add(new Paragraph("Étudiant : " + b.getEtudiant().getNomComplet() + " (" + b.getEtudiant().getMatricule() + ")"));
        document.add(new Paragraph("Classe : " + b.getClasse().getNom()));
        document.add(new Paragraph("\n"));

        // Table des notes
        Table table = new Table(4);
        table.addHeaderCell("Matière");
        table.addHeaderCell("Coefficient");
        table.addHeaderCell("Moyenne / 20");
        table.addHeaderCell("Rang");

        Map<Integer, Double> moyennes = moyenneService.calculerMoyennesToutesMatieres(b.getEtudiant().getId(), b.getPeriode(), b.getAnneeScolaire());
        
        for (Map.Entry<Integer, Double> entry : moyennes.entrySet()) {
            Matiere m = matiereDAO.trouverParId(entry.getKey());
            Rang rm = rangDAO.trouverRangMatiere(b.getEtudiant().getId(), m.getId(), b.getPeriode(), b.getAnneeScolaire());
            
            table.addCell(new Cell().add(new Paragraph(m.getNom())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(m.getCoefficient()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(entry.getValue()))));
            table.addCell(new Cell().add(new Paragraph(rm != null ? String.valueOf(rm.getRang()) : "-")));
        }
        document.add(table);

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("MOYENNE GÉNÉRALE : " + b.getMoyenneGenerale() + " / 20").setBold());
        document.add(new Paragraph("RANG : " + (b.getRang() > 0 ? b.getRang() + " / " + (r != null ? r.getEffectif() : "?") : "-")));
        document.add(new Paragraph("APPRÉCIATION : " + b.getAppreciation()).setItalic());

        document.close();
    }
}
