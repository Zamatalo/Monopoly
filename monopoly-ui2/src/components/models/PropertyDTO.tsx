import {PropertyNames} from "../utils/constants";

export interface PropertyDTO {
    propertyName: PropertyNames;
    cost: number;
    rent: number;
    upgradable: boolean;
}