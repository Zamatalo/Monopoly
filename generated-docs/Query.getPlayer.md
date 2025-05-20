# Query.getPlayer: PlayerDTO
                 
## Arguments
| Name | Description | Required | Type |
| :--- | :---------- | :------: | :--: |
| playerId |  | ✅ | ID! |
            
## Example
```graphql
{
  getPlayer(playerId: "random12345") {
    playerId
    playerName
    color
    balance
    position
    inJail
    ownedProperties
  }
}

```