# Trame de Soutenance : DDDriveByeBye

## Slide 1 – Titre & Introduction (Quentin)
**Titre :** DDDriveByeBye – Plateforme de Mobilité Hybride (VTC & Covoiturage) pilotée par le domaine (DDD)

**Sous-titre :** Comment le DDD réconcilie le temps réel du VTC à la demande et la planification du covoiturage dans un Monolithe Modulaire.

**Accroche :**
* Un domaine complexe par nature : deux modèles économiques opposés (immédiat vs planifié) qui cohabitent.
* Des contraintes réglementaires et tarifaires qui changent radicalement d'une ville à l'autre.

**Objectif de la présentation :**
* Montrer notre découpage en 9 Bounded Contexts (1 Core, 5 Supporting, 3 Generic).
* Expliquer nos choix tactiques (Monolithe Modulaire, isolation JPA/Domaine, pipeline asynchrone *BullMQ-like* en Spring) pour protéger le cœur de métier.

---

## Slide 2 – Pourquoi DDD plutôt que des microservices CRUD ? (Alexy)
**La Complexité métier :**
* Dualité des profils : Un compte *Individual* peut être passager et conducteur, mais un compte *Professional* est strictement VTC.
* Invariants d'états stricts : Un trajet ne peut pas être payé s'il n'a pas été "Picked Up".

**Le Risque sans DDD :**
* Avoir un objet "Trajet" obèse (Big Ball of Mud) qui mélange la géolocalisation, les calculs de prix et les statuts.

**Ce que DDD nous a apporté :**
* Un langage ubiquitaire partagé.
* Une protection de la logique de cycle de vie du trajet (notre Core Domain) via des frontières étanches.

---

## Slide 3 – Strategic Design : La Context Map (Damien)
*Représentation graphique : les 9 Bounded Contexts (cf. `documentation/context_map.png`).*

**Les Bounded Contexts (BC) :**
* **Core Domain ⭐**
  * BC1 – Ride Management
* **Supporting**
  * BC2 – Matching
  * BC3 – Pricing
  * BC4 – User Management
  * BC5 – Territorial Configuration
  * BC6 – Reputation
* **Generic**
  * BC7 – Geolocation & Routing
  * BC8 – Notification
  * BC9 – Payment

**Les Relations (Intégration) :**
* **Partnership :** Ride Management ↔ Matching. Couplage événementiel bidirectionnel (`RideRequestedEvent` consommé par Matching, `MatchFoundEvent` consommé par Ride). Ils échouent ensemble : si le matching tombe, plus aucun trajet n'aboutit ; si Ride tombe, le matching n'a aucun déclencheur.
* **Customer-Supplier :** Matching/Ride/Pricing → Territorial Configuration et User Management ; Reputation → User Management. Lectures unidirectionnelles, pas de partage de fatalité.
* **Anti-Corruption Layer :** Matching → Geolocation via `GeolocationPort` + `StubGeolocationAdapter` (idem pour Pricing à terme). Le port isole le cœur du SDK / du provider externe.
* **Conformist :** Ride → Payment (on suit la sémantique de Stripe quand on l'intégrera).
* **Open Host Service / Published Language :** Ride → Notification (événements publiés, n'importe quel consommateur peut s'y abonner).
* **Shared Kernel (primitives) :** `Money`, `GeoCoordinates`, `DateRange`, `BaseDomainEvent`, `DomainEventPublisher`. Partagé par tous les BCs ; pas de logique métier dedans.

---

## Slide 4 – Zoom sur le Langage Ubiquitaire (L'Anti-corruption linguistique) (Eva)
*Tableau des distinctions critiques :*

* **Comptes :** `Individual Account` (covoiturage, double rôle) vs `Professional Account` (VTC, un seul rôle).
* **Trajets :** `Ride Request` (initié par le passager) vs `Ride Offer` (initié par un covoitureur).
* **Filtres géographiques :** `Activity Zone` (préférence souple pour les particuliers) vs `Working Zone` (contrainte dure pour les pros).

**Choix technique :** Ce vocabulaire se retrouve à l'identique dans le code Java (ex: entités, records, événements).

---

## Slide 5 – Patterns d'Intégration et Justifications (Eva)
**Territorial Configuration comme fournisseur (Customer-Supplier) :**
* *Problème :* Dupliquer les règles tarifaires et réglementaires (taux au km, licence VTC obligatoire, plafond de surge) dans Pricing et Matching.
* *Solution :* Un BC dédié, `TerritorialConfigurationFacade`. Pricing l'interroge pour les barèmes, Matching pour les contraintes (ex. exiger une licence VTC à Paris).

**Anti-Corruption Layer (ACL) sur l'Infrastructure et les modules sœurs non encore livrés :**
* *Problème :* Dépendre des modèles de Stripe / Google Maps, ou attendre qu'un BC sœur soit prêt avant de pouvoir lancer le nôtre.
* *Solution :* Ports déclarés dans `application/port/` + adaptateurs dans `infrastructure/adapters/`. Aujourd'hui : `MockRoutingAdapter` (Geolocation) + trois stubs côté Matching (`StubGeolocationAdapter`, `StubReputationAdapter`, `StubRideOfferCatalogAdapter`). Ils s'effacent automatiquement sous un profil Spring (`geolocation-real`, etc.) quand l'implémentation arrive.
* `MockPaymentAdapter` est planifié dans la spec ; le module Payment n'est pas encore implémenté.

---

## Slide 6 – Le Cœur du Domaine : Ride Management (Core Domain) (Romain)
**Architecture interne :**
* **Agrégat racine :** `Ride`. Implémente une machine à 9 états via le **State Pattern** : `Requested → Proposed → Accepted → PickedUp → InProgress → Arrived → Finalized`, plus deux branches terminales `Cancelled` et `Incident`.
* **Protection :** Pas de mutation publique de l'état — les transitions passent par des méthodes métier (`propose`, `accept`, `pickUp`, `start`, `arrive`, `finalizeRide`, `cancel`, `reportIncident`) qui délèguent à l'objet `RideState` courant. Une transition illégale lève `InvalidRideOperationException`.
* **Deux initiateurs :** `RideRequest` (initié par le passager — flux VTC + covoiturage à la demande) et `RideOffer` (initié par le conducteur — covoiturage planifié).
* **Couverture de tests :** **100 % lignes + 100 % branches** sur `ridemanagement.domain.**`, vérifié à chaque build par JaCoCo.

---

## Slide 7 – Architecture Technique Tactique (Spring Boot) (Damien)
**Le Monolithe Modulaire (Java 21) :**
* Chaque BC est un module hermétique. Communication uniquement via le dossier `api/` (Facades + DTOs).
* **Isolation Data :** Un schéma PostgreSQL par Bounded Context. Schémas effectivement créés aujourd'hui : `users`, `territory`, `geo`, `ride`, `matching`. Aucune jointure SQL inter-modules — toute donnée d'un autre module passe par sa façade.
* **Hexagonal / Ports & Adapters :** repository = port (dans `domain/`), `JpaXxxRepository` = adapter (dans `infrastructure/persistence/`) ; controller = adapter d'entrée, facade = port d'entrée.

**Purisme DDD :**
* *Séparation Domaine / JPA :* Les entités du domaine sont du Java pur, **aucune annotation Spring ni Hibernate**. Une `RideJpaEntity` distincte vit dans `infrastructure/persistence/` et est mappée à la main dans le repository.
* *Domain Events :* L'agrégat empile ses événements via `addEvent(...)`. Le handler applicatif appelle `pullDomainEvents()` **après** `repository.save(...)` puis publie chaque événement via `SpringDomainEventPublisher` — adossé à `ApplicationEventPublisher` de Spring (pas RabbitMQ : c'est du in-process).

**Pipeline asynchrone (équivalent BullMQ) :**
* `MatchingTriggerListener` : `@Async @EventListener` sur `RideRequestedEvent`, exécuté sur un `matchingTaskExecutor` dédié — c'est la "queue worker" du module.
* `ProposalExpiryScheduler` : à la réception d'un `MatchProposalSentEvent`, planifie un job différé via `TaskScheduler` pour faire expirer la proposition au bout de 30 secondes — l'équivalent du *delayed job* de BullMQ.
* In-process aujourd'hui ; migration vers Redis Streams si on doit passer multi-instance, sans toucher au domaine.

---

## Slide 8 – Leçons Apprises et Conclusion (Quentin)
**Pourquoi nos choix fonctionnent :**
* L'absence d'annotations JPA dans le domaine protège nos règles métier de la complexité technique de la base de données.
* Le découpage en schémas SQL séparés nous permet de basculer vers de vrais Microservices le jour où la charge (ex. Geolocation) le nécessitera — chaque schéma part avec son module sans renommage.
* Le pattern *Ports & Adapters avec stubs profilés* nous a permis d'écrire et de tester Matching **avant** que Geolocation/Reputation/Ride-management ne soient livrés : le module bootait, les tests passaient, le code de production était prêt — il a suffi de remplacer le stub par l'adapter réel.
* JaCoCo verrouille à 100 % le `Core Domain` : impossible de dégrader la couverture sans casser le build.

**Conclusion :** Le DDD était indispensable. Il a permis d'unifier deux modèles économiques (VTC & Covoit) sous le même toit sans que l'un ne corrompe l'autre, tout en isolant la complexité des règles territoriales.

---

## Slide 10 – Questions
*(Temps d'échange)*