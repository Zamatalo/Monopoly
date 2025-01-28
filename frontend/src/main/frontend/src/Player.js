export class Player {
    constructor(id, name, properties = [], model = null) {
        this.id = id;
        this.name = name;
        this.properties = properties;
        this.inJail = false;
        this.model = model;
        this.pos = null;
    }

}
