# Reference tests

Reference inputs must specify: species + form, base stats, league CP cap, max level, encounter IV floor and ranking tie policy.

## Verified reference fixture (checked 2026-10-28)
- External reference: https://db.pokemongohub.net/tools/pvp-iv-checker?id=1
- Pokémon: Bulbasaur (standard), base stats 118 / 111 / 128
- League: Great (1500 CP), max level 50, minimum IV 0
- 15/15/15: rank 1, level 50, CP 1260, HP 120
- 0/15/15: rank 770, level 50, CP 1118, HP 120
- Expected eligible spreads: 4096
- Independent reference uses sequential ranks on ties and prefers higher Attack then HP then CP.

## Precision regression
When several spreads have the same algebraic stat product, multiplying
the three floating-point stats in different orders can yield tiny differences
that wrongly change rank. The ranking comparator uses the integer Attack and
Defense values, integer HP, and decimal CPM squared, rather than direct binary
floating-point product comparisons.

## Not yet certified
Cross-species parity, alternative forms, level-51 Buddy exceptions, evolution
families, uncommon capped spreads, and full 4096-row comparisons need more
golden fixtures before product-wide parity claims.
