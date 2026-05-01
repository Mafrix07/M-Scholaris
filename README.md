# 📚 Scholaris — Système de Gestion Scolaire

> Gérez votre établissement avec simplicité.

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?logo=postgresql)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?logo=apachemaven)
![License](https://img.shields.io/badge/Licence-Académique-green)

---

## 📌 Présentation

**Scholaris** est une application de gestion scolaire développée en Java avec une interface graphique JavaFX. Elle permet de gérer les étudiants, les notes, les bulletins et les utilisateurs d'un établissement scolaire (lycée / collège).

Projet académique réalisé dans le cadre du cours **CSC 301 — Génie Logiciel**, Licence Informatique, Semestre 4.

---

## ✨ Fonctionnalités

### 🔐 Authentification & Rôles
- Connexion sécurisée avec hachage **BCrypt**
- 3 rôles : **Admin**, **Enseignant**, **Étudiant**
- Inscription publique → rôle `etudiant` par défaut
- Seul l'Admin peut promouvoir un utilisateur

### 🖥️ Dashboard Admin
- Statistiques générales (utilisateurs, étudiants, enseignants, classes)
- Gestion des utilisateurs (changer les rôles, désactiver)
- Gestion des classes et des matières
- Génération des bulletins PDF

### 👨‍🏫 Dashboard Enseignant
- Saisie des notes par matière et par période
- Création d'événements (devoirs, examens, TP)
- Consultation des moyennes de sa classe

### 👨‍🎓 Dashboard Étudiant
- Consultation de ses notes et moyennes
- Visualisation de son rang (général + par matière)
- Téléchargement de son bulletin PDF
- Calendrier des prochains examens

---

## 🏗️ Architecture

Le projet suit le pattern **MVC (Modèle - Vue - Contrôleur)** :

```
src/main/
├── java/com/scholaris/
│   ├── model/          → Classes métier (POJOs)
│   ├── dao/            → Accès base de données (JDBC)
│   ├── service/        → Logique métier (calculs, PDF)
│   ├── controller/     → Contrôleurs JavaFX
│   ├── util/           → DBConnection, utilitaires
│   └── main/           → Point d'entrée (ScholarisApp)
└── resources/
    ├── fxml/           → Interfaces JavaFX
    ├── css/            → Styles
    └── images/         → Logo et icônes
```

---

## 🗄️ Base de données

**10 tables PostgreSQL :**

| Table | Description |
|---|---|
| `utilisateur` | Tous les utilisateurs (mère) |
| `etudiant` | Profil étudiant (héritage) |
| `enseignant` | Profil enseignant (héritage) |
| `admin` | Profil admin (héritage) |
| `classe` | Classes scolaires |
| `matiere` | Matières avec coefficient |
| `enseignant_matiere` | Affectations enseignant ↔ matière ↔ classe |
| `note` | Notes avec type d'évaluation |
| `type_evaluation` | DS, Examen, TP, Interro... |
| `evenement` | Calendrier scolaire |
| `bulletin` | Bulletins générés |
| `rang` | Rang général + par matière |

> Stratégie d'héritage : **tables jointes** (table `utilisateur` + tables filles)

---

## ⚙️ Stack technique

| Technologie | Version | Usage |
|---|---|---|
| Java | 17 | Langage principal |
| JavaFX | 21 | Interface graphique |
| PostgreSQL | 15+ | Base de données |
| Maven | 3.9 | Gestion des dépendances |
| iText | 5.5.13 | Génération PDF |
| BCrypt (jBCrypt) | 0.4 | Hachage des mots de passe |
| JDBC | — | Connexion PostgreSQL |

---

## 🚀 Installation & Lancement

### Prérequis
- Java 17+
- Maven 3.9+
- PostgreSQL 15+
- IntelliJ IDEA (recommandé)

### Étape 1 — Cloner le projet
```bash
git clone https://github.com/ton-compte/scholaris.git
cd scholaris
```

### Étape 2 — Créer la base de données
```sql
-- Dans pgAdmin ou psql
CREATE DATABASE scholaris;
```
Puis exécuter le script SQL :
```bash
psql -U postgres -d scholaris -f scholaris_postgresql.sql
```

### Étape 3 — Configurer la connexion
Modifier `src/main/java/com/scholaris/util/DBConnection.java` :
```java
private static final String URL      = "jdbc:postgresql://localhost:5432/scholaris";
private static final String USER     = "postgres";
private static final String PASSWORD = "votre_mot_de_passe";
```

### Étape 4 — Créer le premier Admin
```sql
-- Générer le hash BCrypt depuis GenererHash.java puis :
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role, actif)
VALUES ('Nom', 'Prénom', 'admin@scholaris.tg', '$2a$10$...hash...', 'admin', TRUE);

INSERT INTO admin (utilisateur_id, niveau_acces)
VALUES ((SELECT id FROM utilisateur WHERE email = 'admin@scholaris.tg'), 3);
```

### Étape 5 — Lancer l'application
```bash
mvn javafx:run
```
Ou depuis IntelliJ : **Run → ScholarisApp**

---

## 🎨 Design

| Élément | Couleur |
|---|---|
| Bleu primaire | `#1A73E8` |
| Sidebar | `#1E293B` |
| Fond contenu | `#F8FAFC` |
| Texte sidebar | `#94A3B8` |
| Texte actif | `#FFFFFF` |

---

## 📐 Modèle de données — Héritage

```
Utilisateur (abstraite)
├── Etudiant     → matricule, date_naissance, classe_id
├── Professeur   → specialite
└── Admin        → niveau_acces
```

Les attributs communs (`nom`, `prenom`, `email`, `mot_de_passe`, `role`) sont définis **une seule fois** dans `Utilisateur` et hérités automatiquement.

---

## 🧮 Logique métier

### Calcul de la moyenne par matière
```
Moyenne = Σ(valeur × poids_type_eval) / Σ(poids_type_eval)
```

### Calcul de la moyenne générale
```
Moyenne générale = Σ(moyenne_matière × coefficient) / Σ(coefficient)
```

### Appréciation automatique
| Moyenne | Appréciation |
|---|---|
| ≥ 16 | Très Bien |
| ≥ 14 | Bien |
| ≥ 12 | Assez Bien |
| ≥ 10 | Passable |
| < 10 | Insuffisant |


## 📄 Licence

Projet académique — tous droits réservés.
