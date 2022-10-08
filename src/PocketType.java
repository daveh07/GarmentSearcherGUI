public enum PocketType {
    KANGAROO,PATCH,ZIPPER,SLASH,FAUX,NA;

    public String toString(){
        return switch (this){
            case KANGAROO -> "Kangaroo";
            case FAUX -> "Faux";
            case PATCH -> "Patch";
            case SLASH -> "Slash";
            case ZIPPER -> "Zipper";
            case NA -> "Skip...";
        };
    }
}


