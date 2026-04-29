# Trame de Soutenance : DDDriveByeBye

## Slide 1 – Titre & Introduction
**Titre :** DDDriveByeBye – Plateforme de Mobilité Hybride (VTC & Covoiturage) pilotée par le domaine (DDD)

**Sous-titre :** Comment le DDD réconcilie le temps réel du VTC à la demande et la planification du covoiturage dans un Monolithe Modulaire.

**Accroche :**
* Un domaine complexe par nature : deux modèles économiques opposés (immédiat vs planifié) qui cohabitent.
* Des contraintes réglementaires et tarifaires qui changent radicalement d'une ville à l'autre.

**Objectif de la présentation :**
* Montrer notre découpage en 8 Bounded Contexts.
* Expliquer nos choix tactiques (Monolithe Modulaire, isolation JPA/Domaine) pour protéger le cœur de métier.

---

## Slide 2 – Pourquoi DDD plutôt que des microservices CRUD ?
**La Complexité métier :**
* Dualité des profils : Un compte *Individual* peut être passager et conducteur, mais un compte *Professional* est strictement VTC.
* Invariants d'états stricts : Un trajet ne peut pas être payé s'il n'a pas été "Picked Up".

**Le Risque sans DDD :**
* Avoir un objet "Trajet" obèse (Big Ball of Mud) qui mélange la géolocalisation, les calculs de prix et les statuts.

**Ce que DDD nous a apporté :**
* Un langage ubiquitaire partagé.
* Une protection de la logique de cycle de vie du trajet (notre Core Domain) via des frontières étanches.

---

## Slide 3 – Strategic Design : La Context Map
*Représentation graphique : les 8 Bounded Contexts.*

**Les Bounded Contexts (BC) clés :**
* BC1 – Ride Management (Core Domain) ⭐
* BC2 – Matching (Supporting)
* BC3 – Pricing (Supporting)
* BC4 – Territorial Configuration (Generic/Transverse)
* BC5 – User Management (Supporting)

**Les Relations (Intégration) :**
* **Customer-Supplier :** BC1 (Ride) est le client. Il dicte ses besoins à BC2 (Matching) et BC3 (Pricing).
* **Conformist :** BC2 et BC3 se conforment aux données calculées par la Géolocalisation (ils subissent le monde physique).
* **Shared Kernel :** Partage exclusif de primitives techniques (`Money`, `GeoCoordinates`, `BaseDomainEvent`).

---

## Slide 4 – Zoom sur le Langage Ubiquitaire (L'Anti-corruption linguistique)
*Tableau des distinctions critiques :*

* **Comptes :** `Individual Account` (covoiturage, double rôle) vs `Professional Account` (VTC, un seul rôle).
* **Trajets :** `Ride Request` (initié par le passager) vs `Ride Offer` (initié par un covoitureur).
* **Filtres géographiques :** `Activity Zone` (préférence souple pour les particuliers) vs `Working Zone` (contrainte dure pour les pros).

**Choix technique :** Ce vocabulaire se retrouve à l'identique dans le code Java (ex: entités, records, événements).

---

## Slide 5 – Patterns d'Intégration et Justifications
**Territorial Configuration en Open Host Service (OHS) :**
* *Problème :* Dupliquer les règles de tarification (surge, prix minimum) dans le Pricing et le Matching.
* *Solution :* Un BC dédié expose une API claire. Pricing l'interroge pour le calcul, Matching l'interroge pour les contraintes VTC.

**Anti-Corruption Layer (ACL) sur l'Infrastructure :**
* *Problème :* Dépendre des modèles de Stripe (Paiement) ou Google Maps (Géolocalisation).
* *Solution :* Adaptateurs `MockPaymentAdapter` et `MockRoutingAdapter`. Le domaine reste agnostique.

---

## Slide 6 – Le Cœur du Domaine : Ride Management (Core Domain)
**Architecture interne :**
* **Agrégat racine :** `Ride`. C'est lui qui gère la machine à états (Requested → Proposed → Accepted → Picked Up → Finalized).
* **Protection :** Aucun setter public. Les transitions se font via des méthodes métiers qui valident les invariants. Si la transition est illogique, une exception domaine est levée.
* **Deux flux :** Gère en parallèle la logique du `Immediate Ride` (VTC) et du `Scheduled Ride` (Covoiturage).

---

## Slide 7 – Architecture Technique Tactique (Spring Boot)
**Le Monolithe Modulaire (Java 21) :**
* Chaque BC est un module hermétique. Communication uniquement via le dossier `api/` (Facades).
* **Isolation Data :** Un schéma PostgreSQL par Bounded Context (`ride`, `users`, `pricing`). Aucune jointure SQL inter-modules.

**Purisme DDD (Clean Architecture) :**
* *Séparation Domaine / JPA :* Les entités du domaine n'ont **aucune annotation** Spring ou Hibernate.
* *Domain Events :* L'agrégat empile ses événements. Le pattern `pullDomainEvents()` permet de ne les publier à RabbitMQ/Spring qu'une fois la transaction base de données réussie.

---

## Slide 8 – Traversée d'un Scénario BDD (Gherkin)
*Déroulement (Immediate Ride) :*
1. **User Management :** Alice (Passenger) commande. Bob (Professional Driver) est dans sa `Working Zone`.
2. **Ride Management :** Crée la requête et émet `RideRequestedEvent`.
3. **Pricing & Territorial :** Calculent la `Fare Estimate` selon la ville d'Alice.
4. **Matching :** Trouve Bob grâce à sa position `Geolocation`.
5. **Ride Management :** Valide l'état `Picked Up` puis `Arrived`.
6. **Payment & Reputation :** Débitent le `Final Price` et permettent le `Rating`.

**Message clé :** Le Gherkin prouve que le code reflète exactement le métier. Chaque contexte a agi de manière asynchrone et autonome.

---

## Slide 9 – Leçons Apprises et Conclusion
**Pourquoi nos choix fonctionnent :**
* L'absence d'annotations JPA dans le domaine protège nos règles métier de la complexité technique de la base de données.
* Le découpage en schémas SQL séparés nous permet de basculer vers de vrais Microservices le jour où la charge (ex: Geolocation) le nécessitera, sans rien réécrire.

**Conclusion :** Le DDD était indispensable. Il a permis d'unifier deux modèles économiques (VTC & Covoit) sous le même toit sans que l'un ne corrompe l'autre, tout en isolant la complexité des règles territoriales.

---

## Slide 10 – Questions
*(Temps d'échange)*