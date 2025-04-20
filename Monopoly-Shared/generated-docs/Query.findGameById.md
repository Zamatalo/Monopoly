# Query.findGameById: GameDTO!
                 
## Arguments
| Name | Description | Required | Type |
| :--- | :---------- | :------: | :--: |
| gameId |  | ✅ | ID! |
            
## Example
```graphql
{
  findGameById(gameId: "random12345") {
    gameId
    players
    currentPlayerIndex
    gameState
    createdTime
  }
}

```