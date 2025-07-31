# Query.findGameByPlayerId: GameDTO
                 
## Arguments
| Name | Description | Required | Type |
| :--- | :---------- | :------: | :--: |
| playerId |  | ✅ | ID! |
            
## Example
```graphql
{
  findGameByPlayerId(playerId: "random12345") {
    gameId
    players
    currentPlayerIndex
    gameState
    createdTime
    gameActions
  }
}

```