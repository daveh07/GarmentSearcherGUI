public enum GarmentType {
    T_SHIRT, HOODIE, SELECT_TYPE;

    public String toString() {
        return switch (this) {
            case HOODIE -> "Hoodie";
            case T_SHIRT -> "T-shirt";
            case SELECT_TYPE -> "Select Garment Type";
        };
    }
}
