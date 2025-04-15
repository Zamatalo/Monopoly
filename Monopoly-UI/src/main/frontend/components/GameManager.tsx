// frontend/components/GameManager.ts
import {World} from './World';
import {GameDTO} from './GameDTO';
import {PlayerDTO} from './PlayerDTO';

export class GameManager {
    private static instance: GameManager;
    private world?: World;
    private currentGame?: GameDTO;

    private constructor() {}

    public static getInstance(): GameManager {
        if (!GameManager.instance) {
            GameManager.instance = new GameManager();
        }
        return GameManager.instance;
    }

    public init(container: HTMLDivElement): void {
        if (this.world) return;
        this.world = new World(container);
    }

    public async updateGame(gameUpdate: any): Promise<void> {
        if (!this.world) throw new Error('World not initialized');

        if (!this.currentGame) {
            this.currentGame = new GameDTO({
                gameId: gameUpdate.gameId,
                gameState: gameUpdate.gameState,
                players: gameUpdate.players.map((p: any) => new PlayerDTO(p)),
                currentPlayerIndex: gameUpdate.currentPlayerIndex
            } as GameDTO);
            await this.loadInitialGame();
            return;
        }

        this.currentGame.gameState = gameUpdate.gameState;
        this.currentGame.currentPlayerIndex = gameUpdate.currentPlayerIndex;

        gameUpdate.players.forEach((updatedPlayer: any) => {
            const existingPlayer = this.currentGame!.players.find(p => p.playerId === updatedPlayer.playerId);

            if (existingPlayer) {
                // Обновление позиции
                if (existingPlayer.position !== updatedPlayer.position) {
                    const model = this.findPlayerModel(existingPlayer.playerId);
                    if (model) {
                        existingPlayer.animatePlayerMovement(updatedPlayer.position, model);
                    }
                }

                // Обновление других свойств
                existingPlayer.balance = updatedPlayer.balance;
                existingPlayer.inJail = updatedPlayer.inJail;
            } else {
                // Новый игрок
                const newPlayer = new PlayerDTO(updatedPlayer);
                this.currentGame!.players.push(newPlayer);
                newPlayer.loadPlayerModel(this.world!);
            }
        });
    }

    private async loadInitialGame(): Promise<void> {
        if (!this.currentGame || !this.world) return;

        await this.currentGame.loadBoardModel(this.world);
        for (const player of this.currentGame.players) {
            await player.loadPlayerModel(this.world);
        }
    }

    private findPlayerModel(playerId: string): any | undefined {
        return this.world?.scene.children.find(
            (obj: any) => obj.userData?.playerId === playerId
        );
    }
}