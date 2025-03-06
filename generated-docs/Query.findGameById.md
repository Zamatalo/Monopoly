# Query.findGameById: Game!

## Arguments
| Name | Description | Required | Type |
|:-----|:------------|:--------:|:----:|
| id   |             |    ✅     | ID!  |

## Example
```graphql
{
  findGameById(id: "random12345") {
    gameId
    players
    currentPlayerIndex
    gameState
  }
}

```