# Random-Chunck-Generation

Plugin Spigot 1.21 — à chaque fois qu'un chunk est généré ou chargé pour la première fois, tous ses blocs sont remplacés par un seul type de bloc aléatoire.

## Commandes

| Commande | Description | Permission |
|---|---|---|
| `/rcg reload` | Recharge la configuration et ajoute les clés manquantes | `randomchunks.admin` |
| `/rcg reset <monde>` | Réinitialise les chunks transformés d'un monde | `randomchunks.admin` |
| `/rcg info` | Affiche le nombre de blocs dans le pool et les chunks transformés | `randomchunks.admin` |
| `/rcg pregen start <monde> <rayon> [x z]` | Lance la pré-génération autour du spawn (ou de x/z) | `randomchunks.admin` |
| `/rcg pregen stop` | Annule la pré-génération en cours | `randomchunks.admin` |
| `/rcg pregen status` | Affiche la progression de la pré-génération | `randomchunks.admin` |

> La permission `randomchunks.admin` est attribuée aux ops par défaut.

## Configuration

```yaml
# Restreindre à certains mondes (vide = tous les mondes)
enabled-worlds: []

# Liste de blocs autorisés (vide = tous les blocs valides)
block-pool: []

# Conserver le bedrock lors de la transformation
preserve-bedrock: true

# Plage de hauteur de remplacement
min-y: -64
max-y: 320

# Rayon de protection autour du spawn (en chunks)
spawn-protection-radius: 2

# Conserver les données de chunks transformés entre les redémarrages
persist-data: true

# Nombre de chunks générés par tick lors de la pré-génération
pregen-chunks-per-tick: 10
```

## Pré-génération

La commande `/rcg pregen start` permet de forcer la génération d'une zone de chunks à l'avance, sans attendre que les joueurs l'explorent. Utile pour éviter tout lag à l'exploration.

La progression est affichée dans la console toutes les 100 chunks avec le temps écoulé et l'ETA estimée :

```
[Pregen] Démarrage sur world — 441 chunks à traiter (10 chunks/tick).
[Pregen] world : 100/441 (22%) — 2s écoulées — ETA 7s
[Pregen] world terminé en 9s — 441 chunks générés.
```
