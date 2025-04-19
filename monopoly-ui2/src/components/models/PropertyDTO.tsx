import {PropertyNames} from "../utils/constants";

export class PropertyDTO {
    id: string;
    name: string;
    price: number;
    ownerId: string | null;

    constructor(data: PropertyDTO) {
        this.id = data.id;
        this.name = data.name;
        this.price = data.price;
        this.ownerId = data.ownerId ?? null;
    }

    static fromRaw(raw: any): PropertyDTO {
        return new PropertyDTO(raw);
    }
}
