public enum Neckline {

    CREW, V, SCOOP, HIGH,NA;

    public String toString(){
        return switch (this){
            case V -> "V - neck";
            case CREW -> "Crew neck";
            case HIGH -> "High neck";
            case SCOOP -> "Scoop neck";
            case NA -> "Skip...";
        };
    }

}
