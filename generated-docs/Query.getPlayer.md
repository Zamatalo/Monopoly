# Query.getPlayer: PlayerDTO
                 
## Arguments
| Name | Description | Required | Type |
| :--- | :---------- | :------: | :--: |
| playerId |  | ✅ | ID! |
| gameId |  | ✅ | ID! |
            
## Example
```graphql
{
  getPlayer(playerId: "random12345", gameId: "random12345") {
    playerId
    playerName
    color
    balance
    position
    inJail_Turns
    isBot
    playerState
    playerActions
    ownedProperties
  }
}

```