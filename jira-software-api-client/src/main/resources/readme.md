This api is openapi spec is alternated from the original one. If updating, check if the problems are already fixed,
otherwise
fix them and update the api spec.

## List of changes

- added epic model and epic search call for ```/rest/agile/1.0/epic/search``` because it didn't exist in the original
  openapi spec
- added sprint result model for ```/rest/agile/1.0/board/{boardId}/sprint``` and it's crud ops, because it was missing