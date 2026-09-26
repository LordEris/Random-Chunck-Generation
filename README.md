# Random Chunks

Mod **Fabric pour Minecraft 26.3** : chaque chunk est entièrement fait d'**un seul bloc tiré au
hasard**. Relief, grottes, minerais, arbres et structures gardent leur forme, seul leur matériau
change — un chunk de diamant à côté d'un chunk de laine rose, lui-même à côté d'un chunk de glace.

S'applique à **l'Overworld, le Nether et l'End**, et aux dimensions moddées.

---

## Installation

* Minecraft **26.3** avec **Fabric Loader** 0.19.5 ou plus récent.
* Déposer le `.jar` dans le dossier `mods/`. **Fabric API n'est pas nécessaire.**
* Solo ou serveur. Sur un serveur, seul le serveur a besoin du mod : les joueurs se connectent avec
  un client vanilla.

## Ce qui change / ce qui reste

| Devient le bloc du chunk | Reste tel quel |
|---|---|
| Tout le terrain : pierre, deepslate, terre, sable, netherrack, end stone, soufre… | L'air — grottes, ravins et ciel gardent leur forme |
| Les minerais, les géodes, les fossiles | L'eau et la lave |
| Les arbres, les plantes, les coraux | La bedrock (réglable) |
| Les structures : villages, temples, forteresses, bastions, end cities, camps abandonnés… | Les chunks autour du spawn (réglable) |

Le bloc de chaque chunk est tiré **à partir de la seed du monde** et de la position du chunk : même
seed et même config donnent le même monde, ce qui permet de partager une seed.

## Comment ça marche

Le remplacement a lieu **pendant la génération du chunk**, au début de l'étape `LIGHT` :

```
TERRAIN → FEATURES → INITIALIZE_LIGHT → [ LIGHT ] → SPAWN → FULL
                                          ↑
                                          └── le chunk est rempli avec son bloc ici
```

Minecraft ne lance l'étape `LIGHT` d'un chunk que lorsque ses 8 voisins ont terminé leur décoration
(`FEATURES`), la dernière étape qui a le droit d'écrire chez un voisin. À ce moment, tout ce qui
déborde des chunks d'à côté — une moitié d'arbre, un filon, un morceau de village — est déjà là et
est converti avec le reste. La lumière et les heightmaps sont ensuite calculées sur le chunk final.

Les coffres, fours et autres blocs à inventaire remplacés sont supprimés sans rien lâcher, et les
points d'intérêt des villages (lits, cloches, postes de travail) sont désenregistrés.

## Commandes

| Commande | Description |
|---|---|
| `/randomchunks reload` | Relit la config et reconstruit la liste des blocs |
| `/randomchunks info` | Nombre de blocs possibles, chunks remplis depuis le démarrage, réglages |
| `/randomchunks pregen start <dimension> <rayon> [x z]` | Pré-génère un carré de chunks autour du spawn (ou de x z) |
| `/randomchunks pregen stop` | Annule la pré-génération en cours |
| `/randomchunks pregen status` | Affiche la progression |

`/rcg` est un raccourci de `/randomchunks`. Commandes réservées aux opérateurs (niveau 2),
utilisables aussi depuis la console et les blocs de commande.

## Configuration

Créée au premier lancement dans `config/random-chunks.json`. Les réglages ajoutés par une mise à
jour du mod sont ajoutés automatiquement au fichier existant, avec leur valeur par défaut.

```jsonc
{
  "enabled": true,

  // Dimensions concernées. "*" = toutes, y compris moddées.
  "dimensions": ["*"],

  // Si non vide : les SEULS blocs possibles (ids ou #tags), par ex. ["#minecraft:wool", "minecraft:glass"]
  "blockPool": [],

  // Blocs jamais tirés quand blockPool est vide (ids ou #tags). Voir plus bas.
  "excludedBlocks": ["minecraft:bedrock", "minecraft:barrier", "minecraft:tnt", "#minecraft:replaceable", ...],

  // Laisser la bedrock en place (sol du monde, plafond du Nether)
  "preserveBedrock": true,

  // Hauteurs concernées, ramenées aux limites de chaque dimension
  "minY": -64,
  "maxY": 320,

  // Rayon en chunks laissé vanilla autour du spawn, 0 pour tout remplir
  "spawnProtectionRadius": 2,

  // Chunks demandés par tick pendant /randomchunks pregen
  "pregenChunksPerTick": 10
}
```

Les ids s'écrivent `minecraft:stone` ou simplement `stone` ; les noms en majuscules de l'ancienne
config Spigot (`STONE`) sont aussi acceptés.

### Blocs possibles

Par défaut, **tous les blocs du jeu** peuvent être tirés, sauf :

* **Toujours refusés**, même dans `blockPool` : l'air, les fluides, les blocs à entité (coffres,
  fours, panneaux, bannières, têtes, ruches…), les blocs qui tombent (sable, gravier, béton en
  poudre, enclumes, stalactites) et les blocs sans forme d'objet (torches murales, pots avec plante,
  feu, portails…).
* **La liste `excludedBlocks`**, qui reprend les exclusions du plugin : plantes et fleurs, clôtures,
  murets, vitres, portes, trappes, lits, tapis, rails, boutons, plaques de pression, bougies, torches,
  lanternes, chaînes, barreaux, grilles de cuivre, bourgeons d'améthyste, mousse, bedrock, barrière…
  Elle utilise des **tags** (`#minecraft:fences`, `#minecraft:doors`, `#minecraft:replaceable`…),
  donc les blocs de la même famille ajoutés par les prochaines versions sont exclus d'office.

Compatibilité **26.3** vérifiée pour tous les blocs ajoutés depuis la 1.21 : chêne pâle, résine,
fleurs œil, buissons et herbes sèches, fantôme séché, étagères et objets en cuivre, pissenlit doré,
soufre et cinabre, peuplier, dalles et escaliers de laine et de béton, champignon d'étagère, lit de
paille, arbuste rouge. Les blocs pleins (planches, bûches et feuilles de peuplier, soufre, cinabre,
blocs de résine…) font partie des tirages possibles ; les plantes et les blocs non pleins sont exclus.

La liste des blocs retenus est affichée dans les logs au démarrage, avec un avertissement pour
chaque id ou tag inconnu.

## Pré-génération

`/randomchunks pregen start <dimension> <rayon> [x z]` génère à l'avance un carré de
`(2 × rayon + 1)²` chunks, anneau par anneau depuis le centre. Les chunks sont demandés en
arrière-plan et relâchés dès qu'ils sont générés : le serveur continue de tourner normalement, et la
pré-génération continue même quand plus personne n'est connecté.

```
[Pregen] Starting in minecraft:overworld - 441 chunks to generate (10 per tick).
[Pregen] minecraft:overworld: 100/441 (22%) - 2s elapsed - ETA 7s
[Pregen] minecraft:overworld finished in 9s - 441 chunks generated.
```

## Différences avec l'ancien plugin Spigot

* **Le remplacement se fait à la génération**, plus au premier passage d'un joueur : tout chunk que
  l'on voit est déjà transformé, et la génération ne ralentit plus le serveur au moment où l'on
  entre dans un chunk.
* **Plus de `/rcg reset` ni de `transformed_chunks.yml`** : il n'y a plus rien à mémoriser, un
  nouveau monde repart de zéro tout seul.
* **Le bloc d'un chunk dépend de la seed** au lieu d'un tirage à chaque fois.
* **Nouvelles exclusions par défaut** : la TNT (un chunk de TNT à côté d'un lac de lave peut faire
  tomber le serveur), la neige en couche, la table d'enchantement, le conduit, le cœur de grinceur,
  les chaudrons remplis et les colonnes de bulles, qui passent désormais par les règles ci-dessus.
* **Les feuilles ne pourrissent plus** : elles sont posées persistantes, sinon un chunk de feuilles
  disparaîtrait peu à peu.
* Les messages du mod sont en anglais.

## Limitations connues

* Le mod n'agit que sur les **nouveaux** chunks. Un monde existant garde ses chunks déjà générés.
* Les chunks d'un monde créé avant la 1.18 et remis à niveau (*below-zero retrogen*), ainsi que le
  monde de debug, ne sont jamais touchés.
* Les créatures posées par les structures (villageois, sorcières…) se retrouvent dans le bloc du
  chunk et y étouffent.
* L'eau et la lave restent là où la génération les a mises : elles peuvent couler dans un chunk
  voisin dont le bloc laisse passer les fluides.
* Autour des frontières de chunks, la lumière peut être légèrement fausse quand un voisin a calculé
  la sienne avant le remplacement ; elle se corrige dès qu'un bloc est modifié à proximité.

## Compilation

```bash
./gradlew build
```

Le jar sort dans `build/libs/`. Nécessite un **JDK 25**. Depuis la 26.1, Minecraft n'est plus
obfusqué : le projet n'utilise aucun mapping, les mixins visent directement les noms officiels.

La CI construit le jar, le lance sur un vrai serveur Fabric, pré-génère les trois dimensions et
vérifie dans les fichiers de région sauvegardés que chaque chunk généré est fait d'un seul bloc.

## Licence

**CC BY-NC 4.0** — [texte complet](https://creativecommons.org/licenses/by-nc/4.0/legalcode) ·
[résumé](https://creativecommons.org/licenses/by-nc/4.0/)

Utilisation, modification et redistribution libres tant que c'est **non commercial** et que
LordEris est crédité. Interdit : vendre le mod, vendre des licences ou des clés, l'inclure dans un
modpack payant ou dans un grade de serveur payant.

**Les vidéos sont expressément autorisées**, monétisation comprise — pub, sponsors, adhésions,
dons. La permission porte sur le contenu que tu fais *à propos* du mod, pas sur sa distribution
contre paiement. Voir le fichier [LICENSE](LICENSE).
