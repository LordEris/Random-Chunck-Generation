# Random-Chunck-Generation

Plugin Spigot 1.21 — à chaque fois qu'un joueur entre dans un chunk non transformé, tous les blocs du chunk sont remplacés par un seul type de bloc aléatoire.

## Commandes

| Commande | Description | Permission |
|---|---|---|
| `/rcg reload` | Recharge la configuration | `randomchunks.admin` |
| `/rcg reset <world>` | Réinitialise les chunks transformés d'un monde | `randomchunks.admin` ou op |
| `/rcg info` | Affiche le nombre de blocs dans le pool et les chunks transformés | `randomchunks.admin` |

## Configuration

Le fichier `config.yml` permet de :
- Restreindre le plugin à certains mondes (`enabled-worlds`)
- Définir une liste de blocs personnalisée (`block-pool`)
- Régler la plage de hauteur de remplacement (`min-y` / `max-y`)
- Activer/désactiver la notification au joueur (`notify-player`)
- Désactiver la persistance des données (`persist-data`)

## Build

```bash
mvn package
```

Copier le `.jar` généré dans `target/` dans le dossier `plugins/` du serveur.
