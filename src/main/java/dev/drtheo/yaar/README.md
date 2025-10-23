Missing stuff:
- archetypes

### archetypes
some states should have an ability to be grouped, so they are contained in a sub-list, taking less space as a result.

e.g.

`[-, 0, -, -, 1, -, 2] -> [[0, 1, 2], -, -, -, -]`

if they are exclusive, they shouldnt allocate unnecessary space either:

`[-, 0, -, -, -(1), -, -(2)] -> [-, 0(/1/2), -, -, -]`