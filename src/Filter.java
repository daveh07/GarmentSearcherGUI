public enum Filter {
    GARMENT_TYPE,BRAND, SIZE, MATERIAL, NECKLINE, SLEEVE_TYPE, HOODIE_STYLE, POCKET_TYPE;

    public String toString() {
        return switch (this){
            case GARMENT_TYPE -> "Garment type";
            case BRAND -> "Brand";
            case SIZE -> "Available sizes";
            case MATERIAL -> "Material";
            case NECKLINE -> "Neckline";
            case SLEEVE_TYPE -> "Type of sleeve";
            case HOODIE_STYLE -> "Style of hoodie";
            case POCKET_TYPE -> "Type of pocket";
        };
    }
}
