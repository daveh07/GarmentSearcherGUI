public enum HoodieStyle {
    PULLOVER,ZIP_UP,OVER_SIZED,ATHLETIC,NA;

    public String toString(){
        return switch (this){
            case OVER_SIZED -> "Over-sized";
            case PULLOVER -> "Pull-over";
            case ZIP_UP -> "Zip-up";
            case ATHLETIC -> "Athletic";
            case NA -> "Skip...";
        };
    }
}

