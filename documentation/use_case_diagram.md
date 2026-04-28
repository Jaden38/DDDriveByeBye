# UML Use Case Diagram — VTC & Carpooling Mobility Platform

```mermaid
graph LR
  %% ── Actors ──────────────────────────────────────────────
  Passenger(["👤 Passenger"])
  Driver(["👤 Driver"])
  Admin(["👤 Administrator"])
  PaymentProvider(["⚙️ Payment Provider"])
  Support(["👤 Support Team"])

  %% ── System boundary ─────────────────────────────────────
  subgraph Platform ["VTC & Carpooling Mobility Platform"]

    subgraph RideManagement ["Ride Management"]
      UC1(["Submit Ride Request"])
      UC2(["View Fare Estimate"])
      UC3(["Cancel Ride"])
      UC4(["Track Driver in Real Time"])
      UC5(["Accept Ride Proposal"])
      UC6(["Decline Ride Proposal"])
      UC7(["Confirm Pickup"])
      UC8(["Start Ride"])
      UC9(["Confirm Arrival"])
      UC10(["Report Incident"])
    end

    subgraph MatchingPricing ["Matching & Pricing"]
      UC11(["Match Driver to Request"])
      UC12(["Calculate Dynamic Fare"])
      UC13(["Apply Promotional Offer"])
      UC14(["Apply Surge"])
    end

    subgraph PaymentReputation ["Payment & Reputation"]
      UC15(["Process Payment"])
      UC16(["Manage Payment Methods"])
      UC17(["Rate Driver"])
      UC18(["Rate Passenger"])
      UC19(["Request Refund"])
    end

    subgraph UserAdmin ["User & Administration"]
      UC20(["Register as Driver"])
      UC21(["Manage Vehicle Profile"])
      UC22(["Activate Availability"])
      UC23(["Set Activity Zone"])
      UC24(["Manage Preferences"])
      UC25(["Validate Driver Application"])
      UC26(["Configure Territory"])
      UC27(["Apply / Lift Restriction"])
      UC28(["Handle Incident Report"])
    end

  end

  %% ── Passenger associations ───────────────────────────────
  Passenger --- UC1
  Passenger --- UC2
  Passenger --- UC3
  Passenger --- UC4
  Passenger --- UC16
  Passenger --- UC17
  Passenger --- UC24
  Passenger --- UC13
  Passenger --- UC19

  %% ── Driver associations ──────────────────────────────────
  Driver --- UC5
  Driver --- UC6
  Driver --- UC7
  Driver --- UC8
  Driver --- UC9
  Driver --- UC3
  Driver --- UC10
  Driver --- UC20
  Driver --- UC21
  Driver --- UC22
  Driver --- UC23
  Driver --- UC18
  Driver --- UC24

  %% ── Administrator associations ───────────────────────────
  Admin --- UC25
  Admin --- UC26
  Admin --- UC27

  %% ── Support Team associations ────────────────────────────
  Support --- UC28
  Support --- UC27
  Support --- UC19

  %% ── Payment Provider associations ────────────────────────
  PaymentProvider --- UC15

  %% ── Include relationships ────────────────────────────────
  UC1 -- "«include»" --> UC2
  UC1 -- "«include»" --> UC11
  UC11 -- "«include»" --> UC12
  UC12 -- "«extend»" --> UC14
  UC2 -- "«extend»" --> UC13
  UC9 -- "«include»" --> UC15
  UC15 -- "«include»" --> UC17
  UC15 -- "«include»" --> UC18
  UC10 -- "«include»" --> UC28

  %% ── Styling ──────────────────────────────────────────────
  style RideManagement fill:#dbeafe,stroke:#3b82f6,stroke-width:1px
  style MatchingPricing fill:#dcfce7,stroke:#22c55e,stroke-width:1px
  style PaymentReputation fill:#fef9c3,stroke:#eab308,stroke-width:1px
  style UserAdmin fill:#fce7f3,stroke:#ec4899,stroke-width:1px
```

## Actors

| Actor | Role |
|---|---|
| **Passenger** | Submits ride requests, manages payment and preferences, rates drivers |
| **Driver** | Accepts/declines proposals, manages availability and vehicle profile, rates passengers |
| **Administrator** | Configures territories, validates driver applications, manages restrictions |
| **Support Team** | Handles incident reports, processes refunds, applies restrictions |
| **Payment Provider** | External system (e.g. Stripe) that processes financial transactions |

## Relationship Legend

| Notation | Meaning |
|---|---|
| `«include»` | The base use case always triggers the included use case |
| `«extend»` | The extending use case is triggered only under certain conditions |
| `───` | Association between actor and use case |
