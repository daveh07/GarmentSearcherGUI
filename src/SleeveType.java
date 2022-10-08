public enum SleeveType {
    LONG,SHORT,SLEEVELESS,BAT_WING,PUFFED,NA;

    public String toString(){
        return switch (this){
            case SHORT -> "Short";
            case LONG -> "Long";
            case PUFFED -> "Puffed";
            case BAT_WING -> "Bat-wing";
            case SLEEVELESS -> "Sleeveless";
            case NA -> "Skip...";
        };
    }
}
