import {PropertyNames} from "Frontend/utils/constants";

export interface PropertyDTO {
    propertyName: PropertyNames;
    cost: number;
    rent: number;
    upgradable: boolean;
}