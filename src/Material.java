public enum Material {
    COTTON, WOOL_BLEND, POLYESTER,NA;

    public String toString(){
        return switch (this){
            case COTTON -> "Cotton";
            case POLYESTER -> "Polyester";
            case WOOL_BLEND -> "Wool blend";
            case NA -> "Skip...";
        };
    }
}
