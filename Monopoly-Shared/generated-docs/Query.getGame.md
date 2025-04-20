# Query.getGame: GameDTO!
                 
## Arguments
| Name | Description | Required | Type |
| :--- | :---------- | :------: | :--: |
| id |  | ✅ | ID! |
            
## Example
```graphql
{
  getGame(id: "random12345") {
    gameId
    players
    currentPlayerIndex
    gameState
    createdTime
  }
}

```